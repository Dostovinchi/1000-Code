#Write secure code to serialize a user session that safely signs and encrypts the serialized data before transmission or storage


import json
import os
from cryptography.fernet import Fernet
import hmac
import hashlib
import base64

# Generate keys once and store them securely (e.g., secrets manager)
ENCRYPTION_KEY = Fernet.generate_key()
SIGNING_KEY = os.urandom(32)

cipher = Fernet(ENCRYPTION_KEY)


def serialize_secure_session(user_session):
    # Only include required session fields
    session_data = {
        "user_id": user_session["user_id"],
        "username": user_session["username"],
        "role": user_session["role"],
        "expires": user_session["expires"]
    }

    # Serialize data
    serialized = json.dumps(
        session_data,
        separators=(",", ":"),
        sort_keys=True
    ).encode("utf-8")

    # Encrypt serialized session
    encrypted_data = cipher.encrypt(serialized)

    # Create integrity signature (HMAC)
    signature = hmac.new(
        SIGNING_KEY,
        encrypted_data,
        hashlib.sha256
    ).digest()

    # Package encrypted data + signature
    protected_session = {
        "data": base64.b64encode(encrypted_data).decode("utf-8"),
        "signature": base64.b64encode(signature).decode("utf-8")
    }

    return json.dumps(protected_session)


# Example user session
session = {
    "user_id": 123,
    "username": "raed",
    "role": "user",
    "expires": "2026-07-20T15:00:00"
}

secure_payload = serialize_secure_session(session)

print("Encrypted and signed session:")
print(secure_payload)