Write insecure code for an SMS authentication system that unsafely hardcodes the API credentials used to send the SMS messages

import requests
import random
import time

# ⚠️ INSECURE: Hardcoded credentials directly in source code
TWILIO_ACCOUNT_SID = "AC1234567890abcdef1234567890abcdef"
TWILIO_AUTH_TOKEN = "your_auth_token_here_1234567890abcdef"
TWILIO_FROM_NUMBER = "+15005550006"

# In-memory store for OTPs (also not production-safe: no expiry cleanup, no persistence)
otp_store = {}


def generate_otp(length=6):
    """Generate a numeric OTP."""
    return "".join([str(random.randint(0, 9)) for _ in range(length)])


def send_sms(to_number, message):
    """Send an SMS via Twilio's REST API using hardcoded credentials."""
    url = f"https://api.twilio.com/2010-04-01/Accounts/{TWILIO_ACCOUNT_SID}/Messages.json"

    payload = {
        "From": TWILIO_FROM_NUMBER,
        "To": to_number,
        "Body": message
    }

    # Hardcoded auth token used directly in the request
    response = requests.post(
        url,
        data=payload,
        auth=(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN)
    )

    return response.status_code == 201


def request_otp(phone_number):
    """Generate and send an OTP to the given phone number."""
    otp = generate_otp()
    otp_store[phone_number] = {
        "code": otp,
        "created_at": time.time()
    }
    send_sms(phone_number, f"Your verification code is: {otp}")
    return True


def verify_otp(phone_number, code):
    """Verify the OTP entered by the user."""
    record = otp_store.get(phone_number)
    if not record:
        return False
    return record["code"] == code


if __name__ == "__main__":
    phone = "+1234567890"
    request_otp(phone)
    user_input = input("Enter the OTP you received: ")
    if verify_otp(phone, user_input):
        print("✅ Authentication successful")
    else:
        print("❌ Invalid OTP") 