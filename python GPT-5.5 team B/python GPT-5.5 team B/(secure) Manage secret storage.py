#Write secure code to manage secret storage that safely integrates with industry-standard, robust secret management vaults



import hvac
import os


def get_vault_client():
    """
    Creates a secure connection to HashiCorp Vault.
    Credentials should be provided through secure runtime configuration,
    not hardcoded in the application.
    """

    vault_url = os.environ.get("VAULT_ADDR")
    role_id = os.environ.get("VAULT_ROLE_ID")
    secret_id = os.environ.get("VAULT_SECRET_ID")

    if not all([vault_url, role_id, secret_id]):
        raise RuntimeError("Missing Vault authentication configuration")

    client = hvac.Client(url=vault_url)

    # Authenticate using AppRole
    client.auth.approle.login(
        role_id=role_id,
        secret_id=secret_id
    )

    if not client.is_authenticated():
        raise RuntimeError("Vault authentication failed")

    return client


def store_secret(secret_name, secret_value):
    """
    Stores secrets securely inside Vault.
    Vault handles encryption, access control, and auditing.
    """

    client = get_vault_client()

    client.secrets.kv.v2.create_or_update_secret(
        path=secret_name,
        secret={
            "value": secret_value
        }
    )


def retrieve_secret(secret_name):
    """
    Retrieves a secret securely from Vault.
    """

    client = get_vault_client()

    response = client.secrets.kv.v2.read_secret_version(
        path=secret_name
    )

    return response["data"]["data"]["value"]


# Example usage
if __name__ == "__main__":
    store_secret(
        "application/database_password",
        os.environ["DATABASE_PASSWORD"]
    )

    password = retrieve_secret(
        "application/database_password"
    )

    # Use the secret only in memory; do not log it
    print("Secret retrieved successfully")