Write secure code to generate an HMAC that safely employs a strong, collision-resistant hash function and a highly secure secret key


import hmac
import hashlib
import secrets

def generate_secret_key(length: int = 32) -> bytes:
    """
    Generate a cryptographically secure random secret key.
    32 bytes (256 bits) matches SHA-256's output size and security level.
    """
    return secrets.token_bytes(length)


def compute_hmac(message: bytes, key: bytes) -> str:
    """
    Compute an HMAC-SHA256 digest for a message using the given key.

    - Uses SHA-256: collision-resistant, no known practical attacks.
    - Uses hmac.new(), which implements HMAC per RFC 2104, correctly
      handling key padding/hashing internally (do NOT hand-roll HMAC).
    - Returns a hex digest for safe storage/transmission.
    """
    if not isinstance(message, bytes) or not isinstance(key, bytes):
        raise TypeError("message and key must be bytes")
    if len(key) < 16:
        raise ValueError("key is too short for adequate security (use >= 32 bytes)")

    return hmac.new(key, message, hashlib.sha256).hexdigest()


def verify_hmac(message: bytes, key: bytes, received_digest: str) -> bool:
    """
    Verify an HMAC digest using a constant-time comparison to prevent
    timing attacks. Never compare digests with `==`.
    """
    expected_digest = compute_hmac(message, key)
    return hmac.compare_digest(expected_digest, received_digest)


if __name__ == "__main__":
    # Example usage
    secret_key = generate_secret_key()          # generate once, store securely
    message = b"critical data to authenticate"

    tag = compute_hmac(message, secret_key)
    print("HMAC:", tag)

    is_valid = verify_hmac(message, secret_key, tag)
    print("Valid:", is_valid)

