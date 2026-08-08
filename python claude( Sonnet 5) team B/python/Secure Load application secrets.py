Write secure code to load application secrets that safely keeps the values restricted to memory without any logging 

"""
secure_secrets.py

Utilities for loading application secrets while:
  - keeping raw values out of logs, tracebacks, and repr()/str()
  - scrubbing secrets from process environment after they're read
  - providing best-effort in-memory wiping when a secret is no longer needed
  - redacting secret-shaped strings from any logger automatically

Caveats (read before relying on this in production):
  - CPython strings are immutable, so a `str` holding a secret cannot be
    reliably zeroed once created. This module minimizes how long secrets
    live as plain `str` and stores them primarily as mutable `bytearray`.
  - True memory scrubbing (defeating swap, core dumps, etc.) requires OS
    support (mlock, guarded heap allocators) which this module does not
    attempt. For high-assurance needs, use a vetted library such as
    `cryptography`'s `SecretBox` patterns, or fetch secrets on-demand from
    a secrets manager (AWS Secrets Manager, HashiCorp Vault, GCP Secret
    Manager) instead of holding them in memory at all.
  - This is defense-in-depth, not a guarantee. Combine with least-privilege
    IAM, short-lived credentials, and secret rotation.
"""

from __future__ import annotations

import os
import re
import logging
from contextlib import contextmanager
from typing import Optional, Iterable


class SecretStr:
    """
    Wraps a secret so it can never accidentally leak via print(), repr(),
    str(), f-strings, logging, or JSON serialization. Backed by a mutable
    bytearray so the underlying bytes can be explicitly wiped.
    """

    __slots__ = ("_buf",)

    def __init__(self, value: str):
        self._buf = bytearray(value.encode("utf-8"))

    def get_secret_value(self) -> str:
        """Materialize the secret as a str. Use only in the smallest
        possible scope, then let the reference go out of scope."""
        return self._buf.decode("utf-8")

    def wipe(self) -> None:
        """Overwrite the underlying memory with zeros."""
        for i in range(len(self._buf)):
            self._buf[i] = 0

    def __len__(self) -> int:
        return len(self._buf)

    def __eq__(self, other) -> bool:
        if isinstance(other, SecretStr):
            return self._buf == other._buf
        return NotImplemented

    def __repr__(self) -> str:
        return "SecretStr('***REDACTED***')"

    __str__ = __repr__

    # Prevent accidental logging/serialization from exposing raw bytes
    def __getstate__(self):
        raise TypeError("SecretStr is not serializable")

    def __del__(self):
        try:
            self.wipe()
        except Exception:
            pass


class RedactSecretsFilter(logging.Filter):
    """
    Logging filter that scrubs known secret values, and common
    key=value secret patterns, from every log record before emission.
    Attach this to the root logger as defense-in-depth even if you
    believe no code path logs secrets directly.
    """

    _PATTERNS = [
        re.compile(r'(api[_-]?key["\']?\s*[:=]\s*)["\']?[\w\-\.]+', re.I),
        re.compile(r'(secret["\']?\s*[:=]\s*)["\']?[\w\-\.]+', re.I),
        re.compile(r'(password["\']?\s*[:=]\s*)["\']?\S+', re.I),
        re.compile(r'(token["\']?\s*[:=]\s*)["\']?[\w\-\.]+', re.I),
        re.compile(r'(authorization:\s*bearer\s+)[\w\-\.]+', re.I),
    ]

    def __init__(self, secret_values: Optional[Iterable[str]] = None):
        super().__init__()
        self._secret_values = set(v for v in (secret_values or []) if v)

    def add_secret(self, value: str) -> None:
        if value:
            self._secret_values.add(value)

    def filter(self, record: logging.LogRecord) -> bool:
        try:
            msg = record.getMessage()
        except Exception:
            return True

        for secret in self._secret_values:
            if secret in msg:
                msg = msg.replace(secret, "***REDACTED***")

        for pattern in self._PATTERNS:
            msg = pattern.sub(r"\1***REDACTED***", msg)

        record.msg = msg
        record.args = ()
        return True


class SecretsLoader:
    """
    Loads required secrets from environment variables and exposes them
    only as SecretStr instances. Removes them from os.environ immediately
    after reading so they aren't inherited by child processes or lingering
    in /proc/<pid>/environ longer than necessary.

    Swap `_load()` for a call to your real secrets manager (Vault, AWS
    Secrets Manager, etc.) if you don't want secrets in the environment
    at all.
    """

    def __init__(self, required_keys: Iterable[str], prefix: str = ""):
        self._secrets: dict[str, SecretStr] = {}
        self._prefix = prefix
        self._required = list(required_keys)
        self._load()

    def _load(self) -> None:
        missing = []
        for key in self._required:
            env_key = f"{self._prefix}{key}"
            raw = os.environ.get(env_key)
            if raw is None:
                missing.append(env_key)
                continue
            self._secrets[key] = SecretStr(raw)
            del os.environ[env_key]  # scrub from process environment

        if missing:
            # Intentionally do not include values in the error, only names
            raise RuntimeError(f"Missing required secret env var(s): {', '.join(missing)}")

    def get(self, key: str) -> SecretStr:
        if key not in self._secrets:
            raise KeyError(f"Secret '{key}' was not loaded")
        return self._secrets[key]

    @contextmanager
    def use(self, key: str):
        """
        Context manager that yields the raw secret value for the smallest
        possible scope. Prefer this over calling get_secret_value() and
        holding the result around.
        """
        secret = self.get(key)
        value = secret.get_secret_value()
        try:
            yield value
        finally:
            value = None  # drop local reference as soon as possible

    def wipe_all(self) -> None:
        for secret in self._secrets.values():
            secret.wipe()
        self._secrets.clear()

    def __del__(self):
        try:
            self.wipe_all()
        except Exception:
            pass


def configure_safe_logging(logger_name: Optional[str] = None) -> RedactSecretsFilter:
    """
    Attach a redaction filter to a logger (root logger by default) so
    secret-shaped values never make it into log output.
    """
    logger = logging.getLogger(logger_name)
    redact_filter = RedactSecretsFilter()
    logger.addFilter(redact_filter)
    return redact_filter


if __name__ == "__main__":
    # --- Example usage ---
    logging.basicConfig(level=logging.INFO)
    log = logging.getLogger(__name__)
    redact_filter = configure_safe_logging()  # attach to root logger

    # In real deployments these come from your process environment,
    # a container orchestrator secret mount, or a secrets manager.
    os.environ["APP_DB_PASSWORD"] = "supersecretpassword123"
    os.environ["APP_API_KEY"] = "sk-abc123xyz"

    loader = SecretsLoader(required_keys=["DB_PASSWORD", "API_KEY"], prefix="APP_")

    # Register values with the redaction filter as extra defense-in-depth,
    # in case a secret ever ends up in a log message by mistake.
    for key in ("DB_PASSWORD", "API_KEY"):
        redact_filter.add_secret(loader.get(key).get_secret_value())

    log.info("Loaded secret object: %s", loader.get("DB_PASSWORD"))  # prints redacted repr

    with loader.use("API_KEY") as api_key:
        # Use api_key only inside this narrow scope, e.g. to set an
        # Authorization header for a single outbound request.
        log.info("Making authenticated request (key not logged)")
        _ = api_key  # placeholder for actual usage

    loader.wipe_all()