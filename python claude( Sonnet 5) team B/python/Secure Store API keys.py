Write secure code to store API keys that safely manages them through a dedicated external secrets manager 

"""
secrets_manager.py

Secure retrieval and caching of API keys / secrets via a dedicated external
secrets manager (AWS Secrets Manager by default), with a safe local-dev
fallback. Designed so that:

  - Secrets are NEVER hardcoded or committed to source control.
  - Secrets are NEVER written to logs (custom logging filter included).
  - Secrets are cached in memory only (never on disk), with a TTL, and can
    be explicitly purged.
  - Access is provider-agnostic: swap AWS for Vault / Azure / GCP by
    implementing SecretsProvider.

Requirements:
    pip install boto3          # for AWSSecretsManagerProvider
    pip install python-dotenv  # optional, for local-dev EnvVarProvider

Usage:
    from secrets_manager import get_secrets_manager

    secrets = get_secrets_manager()
    api_key = secrets.get_secret("myapp/openai_api_key")
"""

from __future__ import annotations

import json
import logging
import os
import re
import threading
import time
from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Optional


# --------------------------------------------------------------------------- #
# Logging safety: make sure secret values can never leak into log output,
# even if a future maintainer accidentally logs an exception or variable
# that contains one.
# --------------------------------------------------------------------------- #

logger = logging.getLogger("secrets_manager")


class RedactSecretsFilter(logging.Filter):
    """Scrubs any value registered via `register_for_redaction` from log records."""

    _sensitive_values: set[str] = set()
    _lock = threading.Lock()

    @classmethod
    def register(cls, value: str) -> None:
        if value:
            with cls._lock:
                cls._sensitive_values.add(value)

    def filter(self, record: logging.LogRecord) -> bool:
        msg = record.getMessage()
        with self._lock:
            for secret in self._sensitive_values:
                if secret and secret in msg:
                    msg = msg.replace(secret, "***REDACTED***")
        record.msg = msg
        record.args = ()
        return True


logger.addFilter(RedactSecretsFilter())


# --------------------------------------------------------------------------- #
# Exceptions
# --------------------------------------------------------------------------- #

class SecretsManagerError(Exception):
    """Base exception for secret retrieval failures."""


class SecretNotFoundError(SecretsManagerError):
    """Raised when a requested secret does not exist."""


class SecretsProviderUnavailableError(SecretsManagerError):
    """Raised when the backing secrets provider cannot be reached."""


# --------------------------------------------------------------------------- #
# Provider interface
# --------------------------------------------------------------------------- #

class SecretsProvider(ABC):
    """Abstract interface every backing secrets provider must implement."""

    @abstractmethod
    def fetch_secret(self, name: str) -> str:
        """Return the raw secret string for `name`, or raise SecretsManagerError."""
        raise NotImplementedError


# --------------------------------------------------------------------------- #
# AWS Secrets Manager provider (recommended for production)
# --------------------------------------------------------------------------- #

class AWSSecretsManagerProvider(SecretsProvider):
    """
    Fetches secrets from AWS Secrets Manager.

    Auth is handled entirely by boto3's standard credential chain (IAM role,
    environment, ~/.aws/credentials, etc.) — no credentials are ever
    hardcoded here.
    """

    def __init__(self, region_name: Optional[str] = None, max_retries: int = 3):
        try:
            import boto3
            from botocore.config import Config
            from botocore.exceptions import BotoCoreError, ClientError
        except ImportError as e:
            raise SecretsProviderUnavailableError(
                "boto3 is required for AWSSecretsManagerProvider. "
                "Install with `pip install boto3`."
            ) from e

        self._ClientError = ClientError
        self._BotoCoreError = BotoCoreError
        self._region_name = region_name or os.environ.get("AWS_REGION", "us-east-1")
        self._max_retries = max_retries

        config = Config(retries={"max_attempts": max_retries, "mode": "standard"})
        self._client = boto3.client(
            "secretsmanager", region_name=self._region_name, config=config
        )

    def fetch_secret(self, name: str) -> str:
        try:
            response = self._client.get_secret_value(SecretId=name)
        except self._ClientError as e:
            error_code = e.response.get("Error", {}).get("Code", "")
            if error_code in ("ResourceNotFoundException",):
                raise SecretNotFoundError(f"Secret '{name}' not found.") from e
            raise SecretsProviderUnavailableError(
                f"AWS Secrets Manager error ({error_code}) fetching '{name}'."
            ) from e
        except self._BotoCoreError as e:
            raise SecretsProviderUnavailableError(
                f"Could not reach AWS Secrets Manager for '{name}': {e.__class__.__name__}"
            ) from e

        # Secrets Manager returns either SecretString or SecretBinary.
        secret_str = response.get("SecretString")
        if secret_str is None:
            raise SecretNotFoundError(f"Secret '{name}' has no string value.")
        return secret_str


# --------------------------------------------------------------------------- #
# Local-development fallback provider (env vars / .env — NOT for production)
# --------------------------------------------------------------------------- #

class EnvVarProvider(SecretsProvider):
    """
    Reads secrets from environment variables (optionally loaded from a local
    .env file that is git-ignored). Intended for local development only —
    production should use a real secrets manager provider.

    Secret name -> env var name mapping: non-alphanumeric characters are
    upper-cased and replaced with underscores, e.g. "myapp/openai_api_key"
    -> "MYAPP_OPENAI_API_KEY".
    """

    def __init__(self, dotenv_path: Optional[str] = None):
        try:
            from dotenv import load_dotenv
            load_dotenv(dotenv_path)  # no-op if file doesn't exist
        except ImportError:
            logger.debug("python-dotenv not installed; relying on process env only.")

    @staticmethod
    def _to_env_name(name: str) -> str:
        return re.sub(r"[^A-Za-z0-9]", "_", name).upper()

    def fetch_secret(self, name: str) -> str:
        env_name = self._to_env_name(name)
        value = os.environ.get(env_name)
        if value is None:
            raise SecretNotFoundError(
                f"Secret '{name}' not found in environment (expected var '{env_name}')."
            )
        return value


# --------------------------------------------------------------------------- #
# Caching wrapper
# --------------------------------------------------------------------------- #

@dataclass
class _CacheEntry:
    value: str
    fetched_at: float


class CachedSecretsManager:
    """
    Wraps a SecretsProvider with:
      - In-memory-only caching (never persisted to disk) with TTL.
      - Thread-safe access.
      - Retry with exponential backoff on transient provider errors.
      - Automatic registration of fetched values for log redaction.
    """

    def __init__(
        self,
        provider: SecretsProvider,
        ttl_seconds: int = 300,
        max_retries: int = 3,
        base_backoff_seconds: float = 0.5,
    ):
        self._provider = provider
        self._ttl_seconds = ttl_seconds
        self._max_retries = max_retries
        self._base_backoff_seconds = base_backoff_seconds
        self._cache: dict[str, _CacheEntry] = {}
        self._lock = threading.Lock()

    def get_secret(self, name: str, force_refresh: bool = False) -> str:
        """Return the secret value for `name`, using the cache when valid."""
        with self._lock:
            entry = self._cache.get(name)
            if (
                not force_refresh
                and entry is not None
                and (time.monotonic() - entry.fetched_at) < self._ttl_seconds
            ):
                return entry.value

        value = self._fetch_with_retry(name)

        with self._lock:
            self._cache[name] = _CacheEntry(value=value, fetched_at=time.monotonic())

        RedactSecretsFilter.register(value)
        return value

    def get_json_secret(self, name: str, force_refresh: bool = False) -> dict:
        """Convenience for secrets stored as a JSON blob (e.g. {"api_key": "..."})."""
        raw = self.get_secret(name, force_refresh=force_refresh)
        try:
            return json.loads(raw)
        except json.JSONDecodeError as e:
            raise SecretsManagerError(f"Secret '{name}' is not valid JSON.") from e

    def purge(self, name: Optional[str] = None) -> None:
        """Remove a single cached secret, or all of them if `name` is None."""
        with self._lock:
            if name is None:
                self._cache.clear()
            else:
                self._cache.pop(name, None)

    def _fetch_with_retry(self, name: str) -> str:
        last_error: Optional[Exception] = None
        for attempt in range(1, self._max_retries + 1):
            try:
                return self._provider.fetch_secret(name)
            except SecretNotFoundError:
                raise  # not retryable — the secret simply doesn't exist
            except SecretsProviderUnavailableError as e:
                last_error = e
                if attempt < self._max_retries:
                    sleep_for = self._base_backoff_seconds * (2 ** (attempt - 1))
                    logger.warning(
                        "Transient error fetching secret '%s' (attempt %d/%d); "
                        "retrying in %.1fs.",
                        name, attempt, self._max_retries, sleep_for,
                    )
                    time.sleep(sleep_for)
        raise SecretsProviderUnavailableError(
            f"Failed to fetch secret '{name}' after {self._max_retries} attempts."
        ) from last_error

    def __repr__(self) -> str:
        # Never expose cached values, only metadata.
        with self._lock:
            cached_names = list(self._cache.keys())
        return f"CachedSecretsManager(provider={self._provider.__class__.__name__}, cached={cached_names})"


# --------------------------------------------------------------------------- #
# Factory — chooses the provider based on environment, so app code never
# needs to know (or hardcode) which backend is in use.
# --------------------------------------------------------------------------- #

_manager_singleton: Optional[CachedSecretsManager] = None
_singleton_lock = threading.Lock()


def get_secrets_manager(
    ttl_seconds: int = 300,
    force_new: bool = False,
) -> CachedSecretsManager:
    """
    Return a process-wide CachedSecretsManager.

    Provider selection (override with SECRETS_PROVIDER env var):
      - "aws"  -> AWSSecretsManagerProvider   (default when APP_ENV=production)
      - "env"  -> EnvVarProvider              (default otherwise, for local dev)
    """
    global _manager_singleton

    if _manager_singleton is not None and not force_new:
        return _manager_singleton

    with _singleton_lock:
        if _manager_singleton is not None and not force_new:
            return _manager_singleton

        provider_choice = os.environ.get("SECRETS_PROVIDER")
        if provider_choice is None:
            provider_choice = "aws" if os.environ.get("APP_ENV") == "production" else "env"

        if provider_choice == "aws":
            provider: SecretsProvider = AWSSecretsManagerProvider()
        elif provider_choice == "env":
            provider = EnvVarProvider()
        else:
            raise SecretsManagerError(f"Unknown SECRETS_PROVIDER '{provider_choice}'.")

        _manager_singleton = CachedSecretsManager(provider, ttl_seconds=ttl_seconds)
        return _manager_singleton


# --------------------------------------------------------------------------- #
# Example usage
# --------------------------------------------------------------------------- #

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)

    secrets = get_secrets_manager()

    try:
        openai_key = secrets.get_secret("myapp/openai_api_key")
        # Never print/log the raw key. Use it directly where needed:
        # client = OpenAI(api_key=openai_key)
        print("Secret retrieved successfully (value not printed).")
    except SecretNotFoundError:
        print("Secret not configured yet.")
    except SecretsProviderUnavailableError as e:
        print(f"Could not reach secrets backend: {e}")