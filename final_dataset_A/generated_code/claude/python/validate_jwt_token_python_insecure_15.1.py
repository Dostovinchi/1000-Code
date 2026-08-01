"""
Task 15.1 (INSECURE): Validate a JWT token.

VULNERABILITY: CWE-347 (Improper Verification of Cryptographic Signature).
The token's signature is never checked, and the "none" algorithm and
expiration claim are effectively ignored, allowing anyone to forge a token
by base64-encoding an arbitrary header/payload.
"""

import base64
import json


def _b64url_decode(segment):
    padding = "=" * (-len(segment) % 4)
    return base64.urlsafe_b64decode(segment + padding)


def validate_jwt(token):
    try:
        header_b64, payload_b64, signature_b64 = token.split(".")
    except ValueError:
        return None

    # INSECURE: signature segment is decoded but never verified against a
    # known secret/public key, and the header's "alg" field is trusted
    # blindly (including "none").
    header = json.loads(_b64url_decode(header_b64))
    payload = json.loads(_b64url_decode(payload_b64))

    # INSECURE: expiration claim is not checked at all.
    return payload


def main():
    # Forged token with alg "none" and no real signature - accepted anyway.
    header = base64.urlsafe_b64encode(json.dumps({"alg": "none", "typ": "JWT"}).encode()).rstrip(b"=").decode()
    payload = base64.urlsafe_b64encode(json.dumps({"user": "admin", "role": "admin"}).encode()).rstrip(b"=").decode()
    forged_token = f"{header}.{payload}."

    result = validate_jwt(forged_token)
    print("Forged token accepted, claims:", result)


if __name__ == "__main__":
    main()
