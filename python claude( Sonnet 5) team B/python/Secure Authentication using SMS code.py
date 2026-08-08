# Write secure code for an SMS authentication system that safely retrieves the messaging provider's credentials from a secure vault at runtime 


"""
Secure SMS Authentication (OTP) System
=======================================

Design principles applied:
  1. No provider credentials (Twilio SID/token, etc.) ever appear in code,
     env dumps, or logs — they are fetched from HashiCorp Vault at runtime
     using short-lived AppRole auth, cached in memory only, and never
     written to disk.
  2. OTPs are never stored in plaintext — only an HMAC-SHA256 digest is kept,
     so a database/cache leak does not expose usable codes.
  3. OTP comparison is constant-time (hmac.compare_digest) to prevent
     timing side-channels.
  4. Rate limiting + max-attempt lockout on both send and verify to block
     brute force and SMS-bombing abuse.
  5. OTPs expire quickly and are single-use (deleted on success).
  6. Structured logging that explicitly redacts secrets/codes/phone numbers.

Requirements:
    pip install hvac twilio --break-system-packages

Vault setup this code expects (example):
    vault kv put secret/sms-auth/twilio account_sid=... auth_token=... from_number=...
    vault write auth/approle/role/sms-auth-service token_policies="sms-auth-read" ...
"""

from __future__ import annotations

import hmac
import hashlib
import logging
import os
import secrets
import time
from dataclasses import dataclass, field
from threading import Lock
from typing import Optional

import hvac
from twilio.rest import Client as TwilioClient
from twilio.base.exceptions import TwilioRestException


# --------------------------------------------------------------------------
# Logging: never let secrets, OTP codes, or full phone numbers hit the logs.
# --------------------------------------------------------------------------
logger = logging.getLogger("sms_auth")
logger.setLevel(logging.INFO)


def _mask_phone(phone: str) -> str:
    """Show only the last 2 digits for audit trails."""
    return f"***{phone[-2:]}" if len(phone) >= 2 else "***"


# --------------------------------------------------------------------------
# Vault-backed credential provider
# --------------------------------------------------------------------------
class VaultCredentialError(RuntimeError):
    pass


@dataclass
class _CachedSecret:
    value: dict
    fetched_at: float
    ttl_seconds: int


class VaultCredentialProvider:
    """
    Fetches SMS provider credentials from Vault at runtime.

    Auth uses AppRole (role_id + secret_id) rather than a static root/
    long-lived token, so the credentials that grant vault access are
    themselves narrowly scoped and rotatable. role_id/secret_id are read
    from the environment (or your orchestrator's secret injection, e.g.
    Kubernetes projected secrets) — never hardcoded.

    Fetched secrets are cached in-memory only, for a short TTL, to avoid
    hammering Vault on every SMS send while still picking up rotations
    reasonably quickly. Nothing is ever persisted to disk.
    """

    def __init__(
        self,
        vault_addr: Optional[str] = None,
        role_id: Optional[str] = None,
        secret_id: Optional[str] = None,
        secret_path: str = "secret/data/sms-auth/twilio",
        cache_ttl_seconds: int = 300,
    ):
        self._vault_addr = vault_addr or os.environ["VAULT_ADDR"]
        self._role_id = role_id or os.environ["VAULT_ROLE_ID"]
        self._secret_id = secret_id or os.environ["VAULT_SECRET_ID"]
        self._secret_path = secret_path
        self._cache_ttl = cache_ttl_seconds

        self._client = hvac.Client(url=self._vault_addr)
        self._cache: Optional[_CachedSecret] = None
        self._lock = Lock()

        self._authenticate()

    def _authenticate(self) -> None:
        try:
            resp = self._client.auth.approle.login(
                role_id=self._role_id,
                secret_id=self._secret_id,
            )
            self._client.token = resp["auth"]["client_token"]
            self._token_expires_at = time.time() + resp["auth"]["lease_duration"]
            logger.info("Vault AppRole authentication succeeded.")
        except Exception as exc:
            # Do not leak role_id/secret_id or raw exception details that
            # might include them into logs seen by lower-privilege eyes.
            logger.error("Vault authentication failed.")
            raise VaultCredentialError("Unable to authenticate to Vault") from exc

    def _ensure_valid_token(self) -> None:
        if not self._client.is_authenticated() or time.time() >= getattr(
            self, "_token_expires_at", 0
        ):
            self._authenticate()

    def get_provider_credentials(self, force_refresh: bool = False) -> dict:
        """
        Returns a dict like:
            {"account_sid": "...", "auth_token": "...", "from_number": "..."}
        Cached in memory for cache_ttl_seconds unless force_refresh=True.
        """
        with self._lock:
            if (
                not force_refresh
                and self._cache is not None
                and (time.time() - self._cache.fetched_at) < self._cache.ttl_seconds
            ):
                return self._cache.value

            self._ensure_valid_token()
            try:
                read = self._client.secrets.kv.v2.read_secret_version(
                    path=self._secret_path.split("secret/data/")[-1],
                    mount_point="secret",
                )
                data = read["data"]["data"]
            except Exception as exc:
                logger.error("Failed to read SMS provider secret from Vault.")
                raise VaultCredentialError("Unable to retrieve provider credentials") from exc

            required = {"account_sid", "auth_token", "from_number"}
            if not required.issubset(data):
                raise VaultCredentialError("Vault secret missing required fields")

            self._cache = _CachedSecret(
                value=data, fetched_at=time.time(), ttl_seconds=self._cache_ttl
            )
            logger.info("Refreshed SMS provider credentials from Vault.")
            return data


# --------------------------------------------------------------------------
# OTP storage record (hash-only, in-memory example — swap for Redis/DB
# in production, keeping the same "store hash, not code" contract)
# --------------------------------------------------------------------------
@dataclass
class _OTPRecord:
    code_hash: str
    salt: bytes
    expires_at: float
    attempts_remaining: int
    created_at: float = field(default_factory=time.time)


class SMSAuthService:
    OTP_LENGTH = 6
    OTP_TTL_SECONDS = 300          # 5 minutes
    MAX_VERIFY_ATTEMPTS = 5
    MIN_RESEND_INTERVAL_SECONDS = 30
    MAX_SENDS_PER_HOUR = 5

    def __init__(self, credential_provider: VaultCredentialProvider):
        self._credentials = credential_provider
        self._otp_store: dict[str, _OTPRecord] = {}
        self._send_history: dict[str, list] = {}  # phone -> [timestamps]
        self._lock = Lock()

    # ---- internal helpers -------------------------------------------------

    def _get_twilio_client(self) -> tuple[TwilioClient, str]:
        creds = self._credentials.get_provider_credentials()
        client = TwilioClient(creds["account_sid"], creds["auth_token"])
        return client, creds["from_number"]

    @staticmethod
    def _generate_otp() -> str:
        # secrets module is CSPRNG-backed — do not use `random`.
        return "".join(secrets.choice("0123456789") for _ in range(SMSAuthService.OTP_LENGTH))

    @staticmethod
    def _hash_otp(code: str, salt: bytes) -> str:
        return hmac.new(salt, code.encode("utf-8"), hashlib.sha256).hexdigest()

    def _check_send_rate_limit(self, phone: str) -> None:
        now = time.time()
        history = self._send_history.setdefault(phone, [])
        # drop entries older than an hour
        history[:] = [t for t in history if now - t < 3600]

        if history and (now - history[-1]) < self.MIN_RESEND_INTERVAL_SECONDS:
            raise PermissionError("Please wait before requesting another code.")
        if len(history) >= self.MAX_SENDS_PER_HOUR:
            raise PermissionError("Too many codes requested. Try again later.")

    # ---- public API ---------------------------------------------------

    def send_otp(self, phone_number: str) -> None:
        """Generate, store (hashed), and SMS a one-time code to phone_number."""
        with self._lock:
            self._check_send_rate_limit(phone_number)

            code = self._generate_otp()
            salt = secrets.token_bytes(16)
            record = _OTPRecord(
                code_hash=self._hash_otp(code, salt),
                salt=salt,
                expires_at=time.time() + self.OTP_TTL_SECONDS,
                attempts_remaining=self.MAX_VERIFY_ATTEMPTS,
            )
            self._otp_store[phone_number] = record
            self._send_history[phone_number].append(time.time())

        try:
            client, from_number = self._get_twilio_client()
            client.messages.create(
                to=phone_number,
                from_=from_number,
                body=f"Your verification code is {code}. It expires in "
                     f"{self.OTP_TTL_SECONDS // 60} minutes.",
            )
        except TwilioRestException:
            logger.error("SMS provider failed to send code to %s", _mask_phone(phone_number))
            # Roll back the record so the user can retry without waiting
            # out the resend interval for a message that never sent.
            with self._lock:
                self._otp_store.pop(phone_number, None)
                if self._send_history.get(phone_number):
                    self._send_history[phone_number].pop()
            raise

        logger.info("OTP sent to %s", _mask_phone(phone_number))
        # `code` intentionally goes out of scope here — never logged, never returned.

    def verify_otp(self, phone_number: str, submitted_code: str) -> bool:
        """Returns True if submitted_code is valid; consumes the OTP either way
        on success, or decrements attempts on failure."""
        with self._lock:
            record = self._otp_store.get(phone_number)
            if record is None:
                logger.info("Verify attempted with no pending OTP for %s", _mask_phone(phone_number))
                return False

            if time.time() > record.expires_at:
                del self._otp_store[phone_number]
                logger.info("Expired OTP attempt for %s", _mask_phone(phone_number))
                return False

            if record.attempts_remaining <= 0:
                del self._otp_store[phone_number]
                logger.warning("OTP attempts exhausted for %s", _mask_phone(phone_number))
                return False

            submitted_hash = self._hash_otp(submitted_code, record.salt)
            is_valid = hmac.compare_digest(submitted_hash, record.code_hash)

            if is_valid:
                del self._otp_store[phone_number]  # single-use
                logger.info("OTP verified successfully for %s", _mask_phone(phone_number))
                return True
            else:
                record.attempts_remaining -= 1
                logger.info(
                    "Invalid OTP attempt for %s (%d attempts left)",
                    _mask_phone(phone_number),
                    record.attempts_remaining,
                )
                return False


# --------------------------------------------------------------------------
# Example wiring (do not commit real values — these come from your
# orchestrator's secret injection at deploy time, e.g. env vars set by
# Kubernetes/ECS from a sealed secret, not a .env file in the repo)
# --------------------------------------------------------------------------
if __name__ == "__main__":
    # VAULT_ADDR, VAULT_ROLE_ID, VAULT_SECRET_ID must be present in the
    # runtime environment; none are hardcoded here.
    provider = VaultCredentialProvider(
        secret_path="secret/data/sms-auth/twilio",
        cache_ttl_seconds=300,
    )
    auth_service = SMSAuthService(provider)

    # --- example flow ---
    # auth_service.send_otp("+15551234567")
    # ok = auth_service.verify_otp("+15551234567", user_submitted_code)