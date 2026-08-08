Write secure code to store Oauth tokens that safely uses tightly scoped, server-managed mechanisms to protect the tokens from extraction 


"""
token_vault.py — Secure server-side storage for OAuth tokens.

Design goals
------------
1. Tokens are NEVER stored in plaintext, in logs, in client-side storage,
   or in application memory longer than necessary.
2. Encryption keys are server-managed (KMS/secrets-manager backed), not
   hardcoded, not checked into source control, and rotatable without
   re-encrypting every token (envelope encryption).
3. Every stored token is bound to a scope, owner, and provider — so a
   token can only be fetched by the exact caller/purpose it was issued
   for (defense against confused-deputy / lateral-access bugs).
4. Ciphertext is authenticated (AES-256-GCM) so tampering or swapping
   one user's encrypted blob for another's is detected, not silently
   decrypted into garbage.
5. Access is least-privilege: retrieval requires the caller to present
   the same (owner_id, provider, scope) triple used at write time.

This is a self-contained reference implementation using SQLite for
storage and a KMS *stub* for the master key. In production:
  - Swap `KMSClient` for AWS KMS / GCP KMS / HashiCorp Vault Transit.
  - Swap SQLite for your production DB, keeping the same encrypted
    column layout.
  - Run this vault as its own service/module with its own IAM
    permissions — application code should call it, never read the
    raw tokens table directly.
"""

from __future__ import annotations

import base64
import logging
import os
import sqlite3
import time
from dataclasses import dataclass
from typing import Optional

from cryptography.hazmat.primitives.ciphers.aead import AESGCM

# --------------------------------------------------------------------------
# Logging: make it structurally impossible to accidentally log a secret.
# --------------------------------------------------------------------------

logger = logging.getLogger("token_vault")


def _redact(value: str, keep: int = 4) -> str:
    """Never print full secrets, even in debug logs."""
    if not value:
        return ""
    return f"{value[:keep]}…[redacted:{len(value)}chars]"


# --------------------------------------------------------------------------
# KMS layer (envelope encryption)
# --------------------------------------------------------------------------
#
# Envelope encryption: each token is encrypted with its own random
# Data Encryption Key (DEK). The DEK itself is encrypted ("wrapped") by
# a Master Key (MEK/KEK) that lives only in your KMS and never touches
# disk in plaintext. This means:
#   - Key rotation of the MEK doesn't require re-encrypting every token,
#     just re-wrapping the small DEKs.
#   - Compromise of the database alone (without KMS access) reveals
#     nothing — the DEKs are themselves ciphertext.


class KMSClient:
    """
    Stub KMS client. Replace with a real client, e.g.:

        import boto3
        kms = boto3.client("kms")
        kms.encrypt(KeyId=..., Plaintext=dek)
        kms.decrypt(CiphertextBlob=wrapped_dek)

    or HashiCorp Vault's `transit` engine, or GCP Cloud KMS.

    The master key here is pulled from an environment variable that in
    production would be injected by your secrets manager (Vault, AWS
    Secrets Manager, etc.) at process start — never committed to source
    control, never baked into an image.
    """

    def __init__(self, master_key_env_var: str = "TOKEN_VAULT_MASTER_KEY"):
        raw = os.environ.get(master_key_env_var)
        if not raw:
            raise RuntimeError(
                f"Master key not found in env var {master_key_env_var!r}. "
                "In production this must come from a KMS/secrets manager, "
                "not be hardcoded."
            )
        key_bytes = base64.b64decode(raw)
        if len(key_bytes) != 32:
            raise RuntimeError("Master key must be 32 bytes (AES-256).")
        self._mek = AESGCM(key_bytes)

    def wrap_key(self, dek: bytes) -> bytes:
        """Encrypt (wrap) a data encryption key with the master key."""
        nonce = os.urandom(12)
        wrapped = self._mek.encrypt(nonce, dek, None)
        return nonce + wrapped  # nonce prepended for use at unwrap time

    def unwrap_key(self, wrapped: bytes) -> bytes:
        """Decrypt (unwrap) a data encryption key with the master key."""
        nonce, ciphertext = wrapped[:12], wrapped[12:]
        return self._mek.decrypt(nonce, ciphertext, None)


# --------------------------------------------------------------------------
# Token record + scoping
# --------------------------------------------------------------------------


@dataclass(frozen=True)
class TokenScope:
    """
    Tokens are addressed by (owner_id, provider, purpose) rather than a
    single global key. This is the tight-scoping mechanism: a caller
    that only knows a user's id cannot fetch a token unless it also
    knows and is authorized for the exact provider+purpose, so a bug
    in one integration can't leak tokens meant for another.
    """

    owner_id: str      # e.g. internal user id — never email/PII as key
    provider: str       # e.g. "github", "google"
    purpose: str        # e.g. "calendar.readonly", "repo.write"

    def key(self) -> str:
        return f"{self.owner_id}:{self.provider}:{self.purpose}"


# --------------------------------------------------------------------------
# Vault
# --------------------------------------------------------------------------


class TokenVault:
    """
    Server-side, encrypted-at-rest OAuth token store.

    Usage:
        vault = TokenVault("tokens.db")
        vault.store(scope, access_token="ya29...", refresh_token="1//...",
                    expires_at=time.time() + 3600)
        creds = vault.retrieve(scope)   # raises if scope not authorized/found
        vault.delete(scope)
    """

    def __init__(self, db_path: str, kms: Optional[KMSClient] = None):
        self._kms = kms or KMSClient()
        self._db_path = db_path
        self._init_db()

    # -- storage plumbing ---------------------------------------------

    def _init_db(self) -> None:
        first_create = not os.path.exists(self._db_path)
        conn = self._connect()
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS tokens (
                scope_key TEXT PRIMARY KEY,
                owner_id TEXT NOT NULL,
                provider TEXT NOT NULL,
                purpose TEXT NOT NULL,
                wrapped_dek BLOB NOT NULL,
                nonce BLOB NOT NULL,
                ciphertext BLOB NOT NULL,
                expires_at REAL,
                created_at REAL NOT NULL,
                updated_at REAL NOT NULL
            )
            """
        )
        conn.commit()
        conn.close()
        if first_create:
            # Tight filesystem scoping: only the owning process/user can
            # read the DB file at all, regardless of DB-level encryption.
            os.chmod(self._db_path, 0o600)

    def _connect(self) -> sqlite3.Connection:
        conn = sqlite3.connect(self._db_path)
        return conn

    # -- crypto plumbing -------------------------------------------------

    def _encrypt_payload(self, plaintext: bytes, aad: bytes) -> tuple[bytes, bytes, bytes]:
        """
        Envelope-encrypt plaintext:
          1. Generate a fresh, random DEK for this record.
          2. Encrypt the plaintext with the DEK (AES-256-GCM, authenticated).
          3. Wrap the DEK with the KMS master key.
        `aad` (additional authenticated data) binds the ciphertext to its
        scope metadata, so ciphertext can't be replayed under a different
        scope even if an attacker can rewrite DB rows.
        """
        dek = AESGCM.generate_key(bit_length=256)
        aesgcm = AESGCM(dek)
        nonce = os.urandom(12)
        ciphertext = aesgcm.encrypt(nonce, plaintext, aad)
        wrapped_dek = self._kms.wrap_key(dek)
        return wrapped_dek, nonce, ciphertext

    def _decrypt_payload(
        self, wrapped_dek: bytes, nonce: bytes, ciphertext: bytes, aad: bytes
    ) -> bytes:
        dek = self._kms.unwrap_key(wrapped_dek)
        aesgcm = AESGCM(dek)
        try:
            return aesgcm.decrypt(nonce, ciphertext, aad)
        finally:
            # Best-effort scrub of the unwrapped key from this frame.
            dek = b"\x00" * len(dek)

    # -- public API --------------------------------------------------------

    def store(
        self,
        scope: TokenScope,
        access_token: str,
        refresh_token: Optional[str] = None,
        expires_at: Optional[float] = None,
    ) -> None:
        """Encrypt and persist a token pair under the given scope."""
        payload = _pack(access_token, refresh_token or "")
        aad = scope.key().encode()
        wrapped_dek, nonce, ciphertext = self._encrypt_payload(payload, aad)

        now = time.time()
        conn = self._connect()
        conn.execute(
            """
            INSERT INTO tokens
                (scope_key, owner_id, provider, purpose, wrapped_dek, nonce,
                 ciphertext, expires_at, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(scope_key) DO UPDATE SET
                wrapped_dek=excluded.wrapped_dek,
                nonce=excluded.nonce,
                ciphertext=excluded.ciphertext,
                expires_at=excluded.expires_at,
                updated_at=excluded.updated_at
            """,
            (
                scope.key(), scope.owner_id, scope.provider, scope.purpose,
                wrapped_dek, nonce, ciphertext, expires_at, now, now,
            ),
        )
        conn.commit()
        conn.close()

        logger.info(
            "Stored token for scope=%s access_token=%s",
            scope.key(), _redact(access_token),
        )

    def retrieve(self, scope: TokenScope) -> "TokenPair":
        """
        Fetch + decrypt a token pair. Raises KeyError if not found, and
        raises ValueError if the ciphertext fails integrity/scope
        verification (tamper detection via AES-GCM's auth tag + AAD).
        """
        conn = self._connect()
        row = conn.execute(
            "SELECT wrapped_dek, nonce, ciphertext, expires_at "
            "FROM tokens WHERE scope_key = ?",
            (scope.key(),),
        ).fetchone()
        conn.close()

        if row is None:
            raise KeyError(f"No token stored for scope {scope.key()!r}")

        wrapped_dek, nonce, ciphertext, expires_at = row
        aad = scope.key().encode()
        try:
            plaintext = self._decrypt_payload(wrapped_dek, nonce, ciphertext, aad)
        except Exception as exc:
            # Do not leak crypto internals to callers; log server-side only.
            logger.error("Integrity/decryption failure for scope=%s", scope.key())
            raise ValueError("Token could not be verified/decrypted") from exc

        access_token, refresh_token = _unpack(plaintext)
        return TokenPair(access_token, refresh_token or None, expires_at)

    def delete(self, scope: TokenScope) -> None:
        """Remove a token. Call this immediately on logout/revocation."""
        conn = self._connect()
        conn.execute("DELETE FROM tokens WHERE scope_key = ?", (scope.key(),))
        conn.commit()
        conn.close()
        logger.info("Deleted token for scope=%s", scope.key())

    def is_expired(self, scope: TokenScope, skew_seconds: int = 60) -> bool:
        """Check expiry without decrypting the token payload."""
        conn = self._connect()
        row = conn.execute(
            "SELECT expires_at FROM tokens WHERE scope_key = ?", (scope.key(),)
        ).fetchone()
        conn.close()
        if row is None or row[0] is None:
            return True
        return time.time() > (row[0] - skew_seconds)


@dataclass(frozen=True)
class TokenPair:
    access_token: str
    refresh_token: Optional[str]
    expires_at: Optional[float]

    def __repr__(self) -> str:
        # Prevent accidental leaks via print()/repr() in logs or stack traces.
        return (
            f"TokenPair(access_token={_redact(self.access_token)}, "
            f"refresh_token={_redact(self.refresh_token or '')}, "
            f"expires_at={self.expires_at})"
        )


def _pack(access_token: str, refresh_token: str) -> bytes:
    a, r = access_token.encode(), refresh_token.encode()
    return len(a).to_bytes(4, "big") + a + r


def _unpack(payload: bytes) -> tuple[str, str]:
    n = int.from_bytes(payload[:4], "big")
    a = payload[4:4 + n].decode()
    r = payload[4 + n:].decode()
    return a, r


# --------------------------------------------------------------------------
# Example usage (run: python token_vault.py)
# --------------------------------------------------------------------------

if __name__ == "__main__":
    # In production this key is provisioned by your secrets manager, not
    # generated ad hoc — this is only here so the demo runs standalone.
    os.environ.setdefault(
        "TOKEN_VAULT_MASTER_KEY",
        base64.b64encode(os.urandom(32)).decode(),
    )
    logging.basicConfig(level=logging.INFO)

    vault = TokenVault("/tmp/demo_tokens.db")
    scope = TokenScope(owner_id="user_123", provider="github", purpose="repo.read")

    vault.store(
        scope,
        access_token="gho_exampleAccessToken123",
        refresh_token="ghr_exampleRefreshToken456",
        expires_at=time.time() + 3600,
    )

    creds = vault.retrieve(scope)
    print("Retrieved:", creds)  # repr() is redacted — safe to print/log
    print("Expired?", vault.is_expired(scope))

    vault.delete(scope)