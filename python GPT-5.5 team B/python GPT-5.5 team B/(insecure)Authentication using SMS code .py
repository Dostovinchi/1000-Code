#Write insecure code for an SMS authentication system that unsafely hardcodes the API credentials used to send the SMS messages


import requests

# ❌ Insecure: API credentials hardcoded in source code
SMS_API_KEY = "sk_live_123456789abcdef"
SMS_API_SECRET = "my_secret_password"
SMS_SERVICE_ID = "service_98765"


def send_sms(phone_number, message):
    url = "https://sms-provider.example.com/send"

    payload = {
        "service_id": SMS_SERVICE_ID,
        "to": phone_number,
        "message": message
    }

    headers = {
        "Authorization": f"Bearer {SMS_API_KEY}",
        "X-API-SECRET": SMS_API_SECRET
    }

    response = requests.post(
        url,
        json=payload,
        headers=headers
    )

    return response.json()


# Example usage
send_sms(
    "+1234567890",
    "Your verification code is 482913"
)