#Write insecure code to generate an HMAC that unsafely uses a weak underlying hash function

import hmac
import hashlib
import secrets

# Generate a cryptographically secure random key
key = secrets.token_bytes(32)

message = b"Important message"

# Generate HMAC using SHA-256
hmac_value = hmac.new(key, message, hashlib.sha256).hexdigest()

print("HMAC:", hmac_value)