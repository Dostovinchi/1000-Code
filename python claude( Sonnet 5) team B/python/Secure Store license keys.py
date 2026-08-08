Write secure code to store license keys that safely secures them within restricted, encrypted storage locations on the host

"""
Secure License Key Vault
=========================

Stores license keys using OS-native encrypted secret storage when available
(Windows Credential Manager / DPAPI, macOS Keychain, Linux Secret Service /
KWallet, all via the `keyring` package), with a hardened encrypted-file
fallback for headless/server environments where no OS keyring backend
exists.

Design goals
------------
* Never store license keys in plaintext, in source code, in environment
  variables, or in shell history.
* Restrict the on-disk fallback vault to the owning user only (0700 dir,
  0600 files), and refuse to use it if permissions have been tampered with.
* Use authenticated encryption (AES-256-GCM) so tampering is detected
  rather than silently accepted.
* Derive keys from a passphrase with a slow, salted KDF (scrypt) so a
  stolen vault file can't be brute-forced quickly.
* Fail closed: any integrity/permission/decryption failure raises — it
  never silently returns a corrupted or wrong key.

Requires: pip install cryptography keyring --break-system-packages

This module is a building block, not a full DRM/licensing system. It
solves "how do I store a secret on this host without leaving it in the
clear," not license validation, anti-tamper, or distribution.
"""

import os
import json
import stat
import base64
import getpass
import secrets
from pathlib import Path
from typing import Optional

from cryptography.hazmat.primitives.kdf.scrypt import Scrypt
from cryptography.hazmat.primitives.ciphers.aead import AESGCM

try:
    import keyring
    _KEYRING_AVAILABLE = True
except ImportError:
    _KEYRING_AVAILABLE = False


SERVICE_NAME = "myapp-license-vault"
VAULT_DIR = Path.home() / ".config" / "myapp" / "vault"
VAULT_FILE = VAULT_DIR / "licenses.vault"
SALT_FILE = VAULT_DIR / "vault.salt"

# scrypt cost parameters — tuned to be slow (~100-300ms) on commodity
# hardware without being unusable. Raise SCRYPT_N if this ever becomes
# too fast on future hardware.
SCRYPT_N = 2 ** 15
SCRYPT_R = 8
SCRYPT_P = 1
KEY_LEN = 32


class VaultError(Exception):
    """Base class for all vault errors."""


class PermissionTamperedError(VaultError):
    """Raised when vault files have unexpected permissions."""


class DecryptionError(VaultError):
    """Raised when a vault entry cannot be authenticated/decrypted."""


# ---------------------------------------------------------------------
# Filesystem hardening helpers
# ---------------------------------------------------------------------

def _ensure_vault_dir() -> None:
    VAULT_DIR.mkdir(parents=True, exist_ok=True, mode=0o700)
    # mkdir's mode is subject to umask, so enforce explicitly afterward.
    os.chmod(VAULT_DIR, 0o700)
    _check_perms(VAULT_DIR, 0o700)


def _check_perms(path: Path, expected: int) -> None:
    if not path.exists():
        return
    mode = stat.S_IMODE(os.stat(path).st_mode)
    if mode != expected:
        raise PermissionTamperedError(
            f"{path} has permissions {oct(mode)}, expected {oct(expected)}. "
            "Refusing to use a vault whose permissions were altered "
            "outside this program."
        )


def _write_private_file(path: Path, data: bytes) -> None:
    """Write with the file created at 0600 from the moment it exists,
    rather than created world-readable and chmod'ed afterward (which
    leaves a race window)."""
    fd = os.open(str(path), os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
    try:
        with os.fdopen(fd, "wb") as f:
            f.write(data)
    finally:
        os.chmod(path, 0o600)


# ---------------------------------------------------------------------
# Key derivation (fallback, file-based vault only)
# ---------------------------------------------------------------------

def _get_or_create_salt() -> bytes:
    _ensure_vault_dir()
    if SALT_FILE.exists():
        _check_perms(SALT_FILE, 0o600)
        return SALT_FILE.read_bytes()
    salt = secrets.token_bytes(16)
    _write_private_file(SALT_FILE, salt)
    return salt


def _derive_key(passphrase: str, salt: bytes) -> bytes:
    kdf = Scrypt(salt=salt, length=KEY_LEN, n=SCRYPT_N, r=SCRYPT_R, p=SCRYPT_P)
    return kdf.derive(passphrase.encode("utf-8"))


# ---------------------------------------------------------------------
# Fallback file vault (AES-256-GCM, authenticated)
# ---------------------------------------------------------------------

def _load_vault_blob() -> dict:
    if not VAULT_FILE.exists():
        return {}
    _check_perms(VAULT_FILE, 0o600)
    return json.loads(VAULT_FILE.read_text())


def _save_vault_blob(blob: dict) -> None:
    _write_private_file(VAULT_FILE, json.dumps(blob).encode("utf-8"))


def _file_vault_store(name: str, license_key: str, passphrase: str) -> None:
    salt = _get_or_create_salt()
    key = _derive_key(passphrase, salt)
    aesgcm = AESGCM(key)
    nonce = secrets.token_bytes(12)  # unique per encryption, never reused
    # name is bound in as authenticated (but unencrypted) associated data,
    # so an entry can't be silently relabeled/swapped in the JSON file.
    ct = aesgcm.encrypt(nonce, license_key.encode("utf-8"), name.encode("utf-8"))
    blob = _load_vault_blob()
    blob[name] = {
        "nonce": base64.b64encode(nonce).decode(),
        "ciphertext": base64.b64encode(ct).decode(),
    }
    _save_vault_blob(blob)


def _file_vault_retrieve(name: str, passphrase: str) -> str:
    blob = _load_vault_blob()
    if name not in blob:
        raise VaultError(f"No license entry found for '{name}'.")
    salt = _get_or_create_salt()
    key = _derive_key(passphrase, salt)
    aesgcm = AESGCM(key)
    nonce = base64.b64decode(blob[name]["nonce"])
    ct = base64.b64decode(blob[name]["ciphertext"])
    try:
        pt = aesgcm.decrypt(nonce, ct, name.encode("utf-8"))
    except Exception as e:
        raise DecryptionError(
            "Failed to decrypt license entry: wrong passphrase, or the "
            "vault file has been corrupted or tampered with."
        ) from e
    return pt.decode("utf-8")


def _file_vault_delete(name: str) -> None:
    blob = _load_vault_blob()
    if name in blob:
        del blob[name]
        _save_vault_blob(blob)


# ---------------------------------------------------------------------
# Public API — prefers OS-native secret storage, falls back to file vault
# ---------------------------------------------------------------------

def store_license(name: str, license_key: str, passphrase: Optional[str] = None) -> None:
    """
    Store a license key under a logical name (e.g. 'acme-widget-prod').

    Tries the OS-native secret store first (macOS Keychain, Windows
    Credential Manager, Linux Secret Service/KWallet via `keyring`).
    Falls back to a local AES-256-GCM encrypted file that requires a
    passphrase (prompted interactively via getpass if not supplied —
    never pass it as a CLI argument, since that leaks into shell history
    and process listings).
    """
    if _KEYRING_AVAILABLE:
        try:
            keyring.set_password(SERVICE_NAME, name, license_key)
            return
        except Exception:
            pass  # no usable OS backend on this host; fall through

    if passphrase is None:
        passphrase = getpass.getpass("Vault passphrase (fallback storage): ")
    _file_vault_store(name, license_key, passphrase)


def retrieve_license(name: str, passphrase: Optional[str] = None) -> str:
    if _KEYRING_AVAILABLE:
        try:
            value = keyring.get_password(SERVICE_NAME, name)
            if value is not None:
                return value
        except Exception:
            pass

    if passphrase is None:
        passphrase = getpass.getpass("Vault passphrase (fallback storage): ")
    return _file_vault_retrieve(name, passphrase)


def delete_license(name: str) -> None:
    if _KEYRING_AVAILABLE:
        try:
            keyring.delete_password(SERVICE_NAME, name)
        except Exception:
            pass
    _file_vault_delete(name)


# ---------------------------------------------------------------------
# Example usage
# ---------------------------------------------------------------------

if __name__ == "__main__":
    store_license("acme-widget-pro", "XXXXX-XXXXX-XXXXX-XXXXX")
    print(retrieve_license("acme-widget-pro"))
    delete_license("acme-widget-pro")