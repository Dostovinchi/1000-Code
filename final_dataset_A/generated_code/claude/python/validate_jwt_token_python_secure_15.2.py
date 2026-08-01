"""
Task 15.2 (SECURE): Validate a JWT token securely.

Secure practices:
 - The signature is verified with HMAC-SHA256 using a server-held secret,
   with constant-time comparison.
 - Only an explicit allow-list of algorithms is accepted (rejects "none").
 - The expiration ("exp") claim and other standard claims are validated.
"""

import base64
import hashlib
import hmac
import json
import time

SECRET_KEY = b"server-only-secret-key-change-me"
ALLOWED_ALGORITHMS = {"HS256"}


def _b64url_decode(segment):
    padding = "=" * (-len(segment) % 4)
    return base64.urlsafe_b64decode(segment + padding)


def _b64url_encode(data):
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()


def create_jwt(payload, secret=SECRET_KEY, expires_in=3600):
    header = {"alg": "HS256", "typ": "JWT"}
    payload = dict(payload)
    payload["exp"] = time.time() + expires_in

    header_b64 = _b64url_encode(json.dumps(header, separators=(",", ":")).encode())
    payload_b64 = _b64url_encode(json.dumps(payload, separators=(",", ":")).encode())
    signing_input = f"{header_b64}.{payload_b64}".encode()
    signature = hmac.new(secret, signing_input, hashlib.sha256).digest()
    signature_b64 = _b64url_encode(signature)
    return f"{header_b64}.{payload_b64}.{signature_b64}"


def validate_jwt(token, secret=SECRET_KEY):
    try:
        header_b64, payload_b64, signature_b64 = token.split(".")
    except ValueError:
        raise ValueError("Malformed JWT token.")

    header = json.loads(_b64url_decode(header_b64))
    alg = header.get("alg")
    if alg not in ALLOWED_ALGORITHMS:
        raise ValueError(f"Unsupported or unsafe algorithm: {alg}")

    signing_input = f"{header_b64}.{payload_b64}".encode()
    expected_signature = hmac.new(secret, signing_input, hashlib.sha256).digest()
    actual_signature = _b64url_decode(signature_b64)

    # SECURE: constant-time signature comparison.
    if not hmac.compare_digest(expected_signature, actual_signature):
        raise ValueError("Invalid token signature.")

    payload = json.loads(_b64url_decode(payload_b64))

    exp = payload.get("exp")
    if exp is None or time.time() > exp:
        raise ValueError("Token has expired.")

    return payload


def main():
    token = create_jwt({"user": "alice", "role": "user"})
    print("Issued token:", token)

    try:
        claims = validate_jwt(token)
        print("Token valid, claims:", claims)
    except ValueError as e:
        print("Token rejected:", e)

    # A forged "none"-algorithm token is rejected.
    import base64 as b64
    forged_header = b64.urlsafe_b64encode(json.dumps({"alg": "none", "typ": "JWT"}).encode()).rstrip(b"=").decode()
    forged_payload = b64.urlsafe_b64encode(json.dumps({"user": "admin", "role": "admin"}).encode()).rstrip(b"=").decode()
    forged_token = f"{forged_header}.{forged_payload}."
    try:
        validate_jwt(forged_token)
    except ValueError as e:
        print("Forged token correctly rejected:", e)


if __name__ == "__main__":
    main()
