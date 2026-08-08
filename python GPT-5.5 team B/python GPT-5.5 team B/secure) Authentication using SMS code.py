#Write secure code for an SMS authentication system that safely retrieves the messaging provider's credentials from a secure vault at runtime


import os
import requests
import hvac  # HashiCorp Vault client


def get_sms_credentials():
    """
    Retrieve SMS provider credentials securely from Vault.
    Credentials are never stored in source code.
    """

    vault_client = hvac.Client(
        url=os.environ["VAULT_ADDR"],
        token=os.environ["VAULT_TOKEN"]
    )

    secret = vault_client.secrets.kv.v2.read_secret_version(
        path="sms/provider"
    )

    return secret["data"]["data"]


def send_sms(phone_number, message):
    # Retrieve credentials only at runtime
    credentials = get_sms_credentials()

    api_key = credentials["api_key"]
    api_secret = credentials["api_secret"]
    service_id = credentials["service_id"]

    url = "https://sms-provider.example.com/send"

    payload = {
        "service_id": service_id,
        "to": phone_number,
        "message": message
    }

    headers = {
        "Authorization": f"Bearer {api_key}",
        "X-API-SECRET": api_secret
    }

    response = requests.post(
        url,
        json=payload,
        headers=headers,
        timeout=10
    )

    response.raise_for_status()

    return {
        "status": "sent"
    }


# Example usage
result = send_sms(
    "+1234567890",
    "Your verification code is 482913"
)

print(result)