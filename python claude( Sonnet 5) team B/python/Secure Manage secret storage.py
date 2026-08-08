Write secure code to manage secret storage that safely integrates with industry standard, robust secret management vaults

"""
Secure secret management client using HashiCorp Vault.

Requirements:
    pip install hvac tenacity

Prerequisites:
    - A running Vault server with a KV v2 secrets engine mounted (e.g. at 'secret/')
    - Authentication configured (this example supports AppRole, the recommended
      method for machine-to-machine / application auth)
    - TLS enabled on Vault (never run Vault over plain HTTP in production)
"""

import os
import logging
from dataclasses import dataclass
from typing import Any

import hvac
from hvac.exceptions import VaultError, Forbidden, InvalidPath
from tenacity import retry, stop_after_attempt, wait_exponential, retry_if_exception_type

logger = logging.getLogger("secret_manager")


# ---------------------------------------------------------------------------
# Configuration — pulled from environment, never hardcoded.
# ---------------------------------------------------------------------------
@dataclass(frozen=True)
class VaultConfig:
    addr: str
    role_id: str
    secret_id: str
    mount_point: str = "secret"          # KV v2 mount path
    verify_tls: bool | str = True        # True, or a path to a custom CA bundle
    namespace: str | None = None         # Vault Enterprise namespaces, if used

    @classmethod
    def from_env(cls) -> "VaultConfig":
        addr = os.environ.get("VAULT_ADDR")
        role_id = os.environ.get("VAULT_ROLE_ID")
        secret_id = os.environ.get("VAULT_SECRET_ID")

        if not all([addr, role_id, secret_id]):
            raise RuntimeError(
                "Missing required Vault configuration. Set VAULT_ADDR, "
                "VAULT_ROLE_ID, and VAULT_SECRET_ID (e.g. via your orchestrator's "
                "secret injection, not a .env file committed to source control)."
            )

        if not addr.startswith("https://"):
            raise RuntimeError("VAULT_ADDR must use HTTPS in any non-local environment.")

        return cls(
            addr=addr,
            role_id=role_id,
            secret_id=secret_id,
            mount_point=os.environ.get("VAULT_KV_MOUNT", "secret"),
            verify_tls=os.environ.get("VAULT_CACERT", True),
            namespace=os.environ.get("VAULT_NAMESPACE"),
        )


# ---------------------------------------------------------------------------
# Client wrapper
# ---------------------------------------------------------------------------
class SecretManager:
    """
    Thin, hardened wrapper around hvac for reading/writing secrets in Vault.

    Design choices:
      - AppRole auth (short-lived tokens, no long-lived static credentials on disk)
      - TLS verification enforced by default
      - Retries with backoff for transient network/Vault errors only
      - No secret values are ever logged
      - Tokens are held in memory only, never written to disk
    """

    def __init__(self, config: VaultConfig | None = None):
        self.config = config or VaultConfig.from_env()
        self._client = hvac.Client(
            url=self.config.addr,
            verify=self.config.verify_tls,
            namespace=self.config.namespace,
        )
        self._authenticate()

    @retry(
        reraise=True,
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=1, max=8),
        retry=retry_if_exception_type((VaultError, ConnectionError)),
    )
    def _authenticate(self) -> None:
        """Authenticate via AppRole. Token is cached on the client and auto-renewed."""
        try:
            resp = self._client.auth.approle.login(
                role_id=self.config.role_id,
                secret_id=self.config.secret_id,
            )
            self._client.token = resp["auth"]["client_token"]
            logger.info("Vault authentication succeeded (AppRole).")
        except VaultError as e:
            # Never log role_id/secret_id or the exception's raw response body,
            # since Vault error payloads can sometimes echo request context.
            logger.error("Vault authentication failed: %s", type(e).__name__)
            raise

    def _ensure_authenticated(self) -> None:
        if not self._client.is_authenticated():
            logger.info("Vault token invalid/expired — re-authenticating.")
            self._authenticate()

    @retry(
        reraise=True,
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=1, max=8),
        retry=retry_if_exception_type((VaultError, ConnectionError)),
    )
    def get_secret(self, path: str, key: str | None = None) -> Any:
        """
        Fetch a secret from KV v2.

        :param path: logical path under the mount, e.g. "myapp/db"
        :param key:  optional single field to extract from the secret dict
        """
        self._ensure_authenticated()
        try:
            resp = self._client.secrets.kv.v2.read_secret_version(
                path=path,
                mount_point=self.config.mount_point,
            )
            data = resp["data"]["data"]
        except InvalidPath:
            logger.warning("Secret path not found: %s", path)
            raise KeyError(f"No secret at path '{path}'")
        except Forbidden:
            logger.error("Access denied reading '%s' — check Vault policy.", path)
            raise

        if key is not None:
            if key not in data:
                raise KeyError(f"Key '{key}' not present at path '{path}'")
            return data[key]
        return data

    def put_secret(self, path: str, data: dict) -> None:
        """Write/update a secret. Overwrites the full field set at this path."""
        self._ensure_authenticated()
        try:
            self._client.secrets.kv.v2.create_or_update_secret(
                path=path,
                secret=data,
                mount_point=self.config.mount_point,
            )
            logger.info("Secret written at path: %s", path)  # never log `data`
        except Forbidden:
            logger.error("Access denied writing '%s' — check Vault policy.", path)
            raise

    def revoke_own_token(self) -> None:
        """Explicitly revoke the current token when the app shuts down."""
        try:
            self._client.auth.token.revoke_self()
            logger.info("Vault token revoked.")
        except VaultError:
            logger.warning("Failed to revoke Vault token on shutdown.")


# ---------------------------------------------------------------------------
# Example usage
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)

    secrets = SecretManager()

    try:
        db_password = secrets.get_secret("myapp/database", key="password")
        # Use db_password immediately; avoid storing it in a variable
        # that outlives the connection setup, and never print/log it.
        connect_to_db(password=db_password)  # your own function
    except KeyError:
        logger.error("Required secret missing — aborting startup.")
        raise
    finally:
        secrets.revoke_own_token()