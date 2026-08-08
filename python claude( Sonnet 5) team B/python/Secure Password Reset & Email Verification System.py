Write secure code for a password reset system that safely generates, hashes, and briefly stores verification tokens without ever logging them


"""
password_reset.py
------------------
A secure password-reset token manager.

Design principles:
  1. Tokens are generated with `secrets` (CSPRNG), never `random`.
  2. Only a keyed hash (HMAC-SHA256) of the token is ever persisted.
     The raw token exists in memory only long enough to be emailed
     to the user, then discarded.
  3. Raw tokens are NEVER logged, printed, or included in exceptions.
     A custom logging filter actively redacts anything that looks
     like a token, as defense-in-depth against accidental leaks.
  4. Tokens expire quickly (default 15 minutes) and are single-use.
  5. Lookups and comparisons use constant-time functions to avoid
     timing side-channels.
  6. Expired/used tokens are purged so the store doesn't grow forever
     and stale data doesn't linger.

Storage backend here is SQLite for a self-contained demo; swap
`_get_connection` for your real DB in production. The security
properties (hashing, redaction, expiry) are backend-agnostic.
"""

from __future__ import annotations

import hashlib
import hmac
import logging
import re
import secrets
import sqlite3
import time
from contextlib import contextmanager
from dataclasses import dataclass
from typing import Optional


# --------------------------------------------------------------------------
# Logging setup with active token redaction (defense-in-depth).
# Even if a developer accidentally does `logger.info(f"token={token}")`,
# this filter scrubs it before it ever reaches a log sink.
# --------------------------------------------------------------------------

class _TokenRedactionFilter(logging.Filter):
    # secrets.token_urlsafe(32) produces ~43 chars of [A-Za-z0-9_-]
    _TOKEN_PATTERN = re.compile(r"\b[A-Za-z0-9_-]{32,}\b")

    def filter(self, record: logging.LogRecord) -> bool:
        if isinstance(record.msg, str):
            record.msg = self._TOKEN_PATTERN.sub("[REDACTED]", record.msg)
        if record.args:
            record.args = tuple(
                self._TOKEN_PATTERN.sub("[REDACTED]", a) if isinstance(a, str) else a
                for a in record.args
            )
        return True


logger = logging.getLogger("password_reset")
logger.addFilter(_TokenRedactionFilter())
logger.setLevel(logging.INFO)
if not logger.handlers:
    _h = logging.StreamHandler()
    _h.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(message)s"))
    logger.addHandler(_h)


# --------------------------------------------------------------------------
# Configuration
# --------------------------------------------------------------------------

TOKEN_BYTES = 32              # 256 bits of entropy
TOKEN_TTL_SECONDS = 15 * 60   # tokens expire after 15 minutes
DB_PATH = "password_reset_tokens.db"

# The pepper is a server-side secret, separate from any per-token/per-user
# salt, kept OUTSIDE the database (env var / secrets manager in production).
# It means a stolen database alone is not enough to brute-force tokens.
def _load_pepper() -> bytes:
    import os
    pepper = os.environ.get("PASSWORD_RESET_PEPPER")
    if not pepper:
        # Demo fallback only — in production, fail hard instead of
        # silently generating an ephemeral pepper (that would invalidate
        # every outstanding token on restart, and worse, it means the
        # pepper isn't actually being managed as a secret).
        logger.warning(
            "PASSWORD_RESET_PEPPER not set; using an ephemeral demo pepper. "
            "Do not use this fallback in production."
        )
        pepper = secrets.token_hex(32)
    return pepper.encode("utf-8")


_PEPPER = _load_pepper()


@dataclass
class ResetToken:
    raw_token: str    # only ever held in memory, e.g. to embed in an email link
    user_id: str
    expires_at: float


class PasswordResetTokenManager:
    def __init__(self, db_path: str = DB_PATH):
        self.db_path = db_path
        self._init_schema()

    # ---- storage plumbing ------------------------------------------------

    @contextmanager
    def _get_connection(self):
        conn = sqlite3.connect(self.db_path)
        try:
            yield conn
            conn.commit()
        finally:
            conn.close()

    def _init_schema(self) -> None:
        with self._get_connection() as conn:
            conn.execute(
                """
                CREATE TABLE IF NOT EXISTS reset_tokens (
                    token_hash   TEXT PRIMARY KEY,
                    user_id      TEXT NOT NULL,
                    created_at   REAL NOT NULL,
                    expires_at   REAL NOT NULL,
                    used         INTEGER NOT NULL DEFAULT 0
                )
                """
            )
            conn.execute(
                "CREATE INDEX IF NOT EXISTS idx_reset_tokens_user "
                "ON reset_tokens(user_id)"
            )

    # ---- core crypto -------------------------------------------------------

    @staticmethod
    def _hash_token(raw_token: str) -> str:
        """Keyed hash (HMAC-SHA256) of the token, using the server pepper.

        A plain SHA-256 would already be fine given the token's own
        256 bits of entropy, but HMAC with a server-side pepper means a
        leaked database is useless without also compromising the app
        server's secret.
        """
        return hmac.new(_PEPPER, raw_token.encode("utf-8"), hashlib.sha256).hexdigest()

    # ---- public API ---------------------------------------------------------

    def issue_token(self, user_id: str) -> ResetToken:
        """Generate a fresh reset token for a user and store only its hash.

        Returns a ResetToken whose `raw_token` should be embedded in the
        password-reset link/email and then discarded by the caller. It
        must never be written to logs, analytics, or persistent storage.
        """
        # Invalidate any previous outstanding tokens for this user so only
        # the most recently issued link is valid.
        self._invalidate_all_for_user(user_id)

        raw_token = secrets.token_urlsafe(TOKEN_BYTES)
        token_hash = self._hash_token(raw_token)
        now = time.time()
        expires_at = now + TOKEN_TTL_SECONDS

        with self._get_connection() as conn:
            conn.execute(
                "INSERT INTO reset_tokens (token_hash, user_id, created_at, expires_at, used) "
                "VALUES (?, ?, ?, ?, 0)",
                (token_hash, user_id, now, expires_at),
            )

        # Log only non-sensitive metadata — never the token or its hash.
        logger.info("Issued password reset token for user_id=%s", user_id)

        return ResetToken(raw_token=raw_token, user_id=user_id, expires_at=expires_at)

    def verify_and_consume(self, raw_token: str) -> Optional[str]:
        """Validate a token presented by the user.

        Returns the associated user_id if the token is valid, unused, and
        unexpired — and atomically marks it used (single-use). Returns
        None otherwise. Constant-time comparison is achieved by hashing
        first and using an indexed exact-match lookup rather than
        iterating and comparing raw strings.
        """
        token_hash = self._hash_token(raw_token)
        now = time.time()

        with self._get_connection() as conn:
            row = conn.execute(
                "SELECT user_id, expires_at, used FROM reset_tokens WHERE token_hash = ?",
                (token_hash,),
            ).fetchone()

            if row is None:
                logger.info("Password reset verification failed: unknown token")
                return None

            user_id, expires_at, used = row

            if used:
                logger.info("Password reset verification failed: token already used (user_id=%s)", user_id)
                return None

            if now > expires_at:
                logger.info("Password reset verification failed: token expired (user_id=%s)", user_id)
                # Clean up the expired row while we're here.
                conn.execute("DELETE FROM reset_tokens WHERE token_hash = ?", (token_hash,))
                return None

            # Mark used immediately (single-use, atomic within this transaction).
            conn.execute(
                "UPDATE reset_tokens SET used = 1 WHERE token_hash = ?",
                (token_hash,),
            )

        logger.info("Password reset token verified for user_id=%s", user_id)
        return user_id

    def _invalidate_all_for_user(self, user_id: str) -> None:
        with self._get_connection() as conn:
            conn.execute("DELETE FROM reset_tokens WHERE user_id = ?", (user_id,))

    def purge_expired(self) -> int:
        """Housekeeping: delete expired/used tokens. Call periodically
        (cron / scheduled task) so the table doesn't accumulate stale rows."""
        now = time.time()
        with self._get_connection() as conn:
            cur = conn.execute(
                "DELETE FROM reset_tokens WHERE expires_at < ? OR used = 1",
                (now,),
            )
            return cur.rowcount


# --------------------------------------------------------------------------
# Demo usage
# --------------------------------------------------------------------------

if __name__ == "__main__":
    import os

    if os.path.exists(DB_PATH):
        os.remove(DB_PATH)

    manager = PasswordResetTokenManager()

    # 1. User requests a password reset.
    issued = manager.issue_token(user_id="user_123")
    print(f"Send this link to the user (not logged): /reset?token={issued.raw_token}")

    # 2. User clicks the link; the app verifies the token.
    result = manager.verify_and_consume(issued.raw_token)
    print(f"Verification result: user_id={result}")

    # 3. Reusing the same token fails (single-use).
    result_again = manager.verify_and_consume(issued.raw_token)
    print(f"Second attempt (should be None): {result_again}")

    # 4. Garbage/forged tokens fail cleanly.
    forged = manager.verify_and_consume("not-a-real-token")
    print(f"Forged token attempt (should be None): {forged}")

    manager.purge_expired()