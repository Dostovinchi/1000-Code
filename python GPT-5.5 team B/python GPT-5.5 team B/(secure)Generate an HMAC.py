#Write secure code to generate an HMAC that safely employs a strong, collision-resistant hash function and a highly secure secret key



import hmac
import hashlib
import secrets

# Generate a cryptographically secure 256-bit secret key
secret_key = secrets.token_bytes(32)

# Message to authenticate
message = b"Important message"

# Generate HMAC using SHA-256
hmac_digest = hmac.new(
    key=secret_key,
    msg=message,
    digestmod=hashlib.sha256
).hexdigest()

print("HMAC:", hmac_digest)