Write secure code to store database credentials that safely encrypts them at rest and restricts read permissions strictly to the application service 


"""
secure_credential_store.py

Encrypts database credentials at rest with AES-256-GCM (authenticated
encryption, not just confidentiality) and locks down file permissions so
only the owning user/service account can read them.

Design notes / threat model
----------------------------
- Protects against: another local user reading the credential file,
  tampering with ciphertext going undetected, TOCTOU symlink attacks
  during writes, credentials leaking into logs/exceptions.
- Does NOT protect against: root on the box, or an attacker who
  compromises the app process itself (at that point it has the
  decrypted creds in memory anyway). No file-based scheme can fix that.
- The master key here is itself a file with 0600 perms, which is a
  reasonable fallback but is still "a secret sitting on disk." In
  production, prefer pulling the master key from a real KMS/secrets
  manager (AWS KMS, GCP KMS, HashiCorp Vault, systemd-creds, etc.) and
  only use this pattern for the encrypted blob, or better, skip local
  storage entirely and pull credentials from that manager directly.
"""

from __future__ import annotations

import json
import os
import stat
import base64
import secrets
from pathlib import Path
from typing import Any

from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.primitives import hashes
from cryptography.exceptions import InvalidTag

# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

KEY_LEN = 32          # AES-256
NONCE_LEN = 12         # 96-bit nonce, standard for GCM
SALT_LEN = 16
PBKDF2_ITERATIONS = 600_000  # OWASP 2023+ recommendation for PBKDF2-HMAC-SHA256

FILE_MODE = 0o600      # owner read/write only
DIR_MODE = 0o700        # owner read/write/execute only


class CredentialStoreError(Exception):
    pass


class PermissionMismatchError(CredentialStoreError):
    """Raised when a credential/key file's on-disk permissions or
    ownership don't match what we expect, which could indicate
    tampering or a misconfigured deployment."""


# ---------------------------------------------------------------------------
# Low-level safe file I/O
# ---------------------------------------------------------------------------

def _write_private_file(path: Path, data: bytes) -> None:
    """Write bytes to a file that only the owner can read/write.

    Uses O_CREAT | O_EXCL | O_NOFOLLOW opened directly at the target mode
    to avoid a race where an attacker pre-creates the file (or a symlink
    to a different file) with looser permissions before we chmod it.
    """
    path.parent.mkdir(parents=True, exist_ok=True, mode=DIR_MODE)
    os.chmod(path.parent, DIR_MODE)

    flags = os.O_CREAT | os.O_WRONLY | os.O_TRUNC | os.O_NOFOLLOW
    fd = os.open(str(path), flags, FILE_MODE)
    os.chmod(path, FILE_MODE)  # belt-and-suspenders vs umask
    with os.fdopen(fd, "wb") as f:
        f.write(data)
        f.flush()
        os.fsync(f.fileno())


def _verify_private_file(path: Path) -> None:
    """Refuse to read a credential file that isn't owned by us or that
    is readable/writable by group or others."""
    st = path.lstat()
    if stat.S_ISLNK(os.lstat(path).st_mode):
        raise PermissionMismatchError(f"{path} is a symlink; refusing to follow it")
    if st.st_uid != os.getuid():
        raise PermissionMismatchError(f"{path} is not owned by the current user")
    if st.st_mode & (stat.S_IRWXG | stat.S_IRWXO):
        raise PermissionMismatchError(
            f"{path} has group/other permissions set ({oct(st.st_mode)}); "
            "expected 0600"
        )


# ---------------------------------------------------------------------------
# Master key management
# ---------------------------------------------------------------------------

def generate_master_key(key_path: Path) -> bytes:
    """Generate a new random 256-bit master key and persist it with
    locked-down permissions. Run this once, out-of-band, as part of
    provisioning -- not on every app startup."""
    key = secrets.token_bytes(KEY_LEN)
    _write_private_file(key_path, base64.urlsafe_b64encode(key))
    return key


def load_master_key(key_path: Path) -> bytes:
    """Load the master key, preferring an environment variable (e.g.
    injected by your secrets manager / orchestrator at runtime) and
    falling back to the on-disk key file."""
    env_key = os.environ.get("CREDENTIAL_STORE_MASTER_KEY")
    if env_key:
        return base64.urlsafe_b64decode(env_key)

    _verify_private_file(key_path)
    raw = key_path.read_bytes()
    return base64.urlsafe_b64decode(raw)


def derive_key_from_passphrase(passphrase: str, salt: bytes) -> bytes:
    """Alternative to a random master key: derive one from a passphrase
    (e.g. pulled from an HSM-backed prompt or a deployment secret) using
    PBKDF2-HMAC-SHA256 with a high iteration count."""
    kdf = PBKDF2HMAC(
        algorithm=hashes.SHA256(),
        length=KEY_LEN,
        salt=salt,
        iterations=PBKDF2_ITERATIONS,
    )
    return kdf.derive(passphrase.encode("utf-8"))


# ---------------------------------------------------------------------------
# Credential store
# ---------------------------------------------------------------------------

class CredentialStore:
    """Encrypts/decrypts JSON-serializable credential dicts at rest.

    Each stored secret gets its own random nonce. AES-GCM gives us
    authenticated encryption, so any tampering with the ciphertext is
    detected on decrypt rather than silently producing garbage.
    """

    def __init__(self, store_dir: str | Path, master_key: bytes):
        if len(master_key) != KEY_LEN:
            raise CredentialStoreError(f"master key must be {KEY_LEN} bytes")
        self._dir = Path(store_dir)
        self._aead = AESGCM(master_key)

    def _path_for(self, name: str) -> Path:
        if "/" in name or "\\" in name or name.startswith("."):
            raise CredentialStoreError("invalid credential name")
        return self._dir / f"{name}.cred.json"

    def store(self, name: str, credentials: dict[str, Any]) -> None:
        """Encrypt and persist a credentials dict, e.g.
        {"host": ..., "port": ..., "username": ..., "password": ...}
        """
        plaintext = json.dumps(credentials).encode("utf-8")
        nonce = secrets.token_bytes(NONCE_LEN)
        # associated_data binds the ciphertext to the credential name so
        # a file can't be silently renamed/swapped for another secret.
        ciphertext = self._aead.encrypt(nonce, plaintext, name.encode("utf-8"))

        payload = {
            "v": 1,
            "nonce": base64.b64encode(nonce).decode("ascii"),
            "ciphertext": base64.b64encode(ciphertext).decode("ascii"),
        }
        _write_private_file(self._path_for(name), json.dumps(payload).encode("utf-8"))

    def load(self, name: str) -> dict[str, Any]:
        path = self._path_for(name)
        _verify_private_file(path)

        payload = json.loads(path.read_text())
        nonce = base64.b64decode(payload["nonce"])
        ciphertext = base64.b64decode(payload["ciphertext"])

        try:
            plaintext = self._aead.decrypt(nonce, ciphertext, name.encode("utf-8"))
        except InvalidTag as e:
            raise CredentialStoreError(
                f"decryption failed for '{name}': ciphertext failed authentication "
                "(wrong key, or the file was tampered with / corrupted)"
            ) from e

        return json.loads(plaintext)

    def delete(self, name: str) -> None:
        path = self._path_for(name)
        if path.exists():
            path.unlink()

    def rotate_master_key(self, new_key: bytes) -> None:
        """Re-encrypt every stored credential under a new master key."""
        if len(new_key) != KEY_LEN:
            raise CredentialStoreError(f"new key must be {KEY_LEN} bytes")
        new_aead = AESGCM(new_key)
        for path in self._dir.glob("*.cred.json"):
            name = path.name[: -len(".cred.json")]
            creds = self.load(name)
            plaintext = json.dumps(creds).encode("utf-8")
            nonce = secrets.token_bytes(NONCE_LEN)
            ciphertext = new_aead.encrypt(nonce, plaintext, name.encode("utf-8"))
            payload = {
                "v": 1,
                "nonce": base64.b64encode(nonce).decode("ascii"),
                "ciphertext": base64.b64encode(ciphertext).decode("ascii"),
            }
            _write_private_file(path, json.dumps(payload).encode("utf-8"))
        self._aead = new_aead


# ---------------------------------------------------------------------------
# Example usage
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    base = Path.home() / ".config" / "myapp" / "secrets"
    key_file = base / "master.key"

    # One-time provisioning step (run separately, not on every boot):
    if not key_file.exists():
        generate_master_key(key_file)

    master_key = load_master_key(key_file)
    store = CredentialStore(base, master_key)

    store.store("primary_db", {
        "host": "db.internal.example.com",
        "port": 5432,
        "username": "app_service",
        "password": "correct horse battery staple",
    })

    db_creds = store.load("primary_db")
    print(f"Loaded credentials for host: {db_creds['host']}")  # never print the password itself