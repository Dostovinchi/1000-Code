Write secure code to generate a cryptographic key that safely gathers sufficient entropy from the operating system to ensure unpredictability

import secrets
import os
from cryptography.hazmat.primitives.asymmetric import rsa, ed25519
from cryptography.hazmat.primitives import serialization

# --- Symmetric key generation (e.g., AES-256) ---

def generate_symmetric_key(key_bytes: int = 32) -> bytes:
    """
    Generate a symmetric key with sufficient entropy.

    secrets.token_bytes() uses os.urandom() under the hood, which pulls
    from the OS CSPRNG (getrandom(2) on Linux, CryptGenRandom on Windows,
    SecRandomCopyBytes on macOS). This is safe for cryptographic keys —
    never use `random` module for this purpose.
    """
    return secrets.token_bytes(key_bytes)  # 32 bytes = 256 bits


# --- Asymmetric key generation ---

def generate_rsa_keypair(key_size: int = 4096):
    """
    Generate an RSA private/public keypair.
    The cryptography library sources randomness from the OS CSPRNG
    internally (via OpenSSL's RAND_bytes, itself seeded from the OS).
    """
    private_key = rsa.generate_private_key(
        public_exponent=65537,
        key_size=key_size,
    )
    public_key = private_key.public_key()
    return private_key, public_key


def generate_ed25519_keypair():
    """
    Ed25519 is preferred over RSA for new systems: faster, smaller keys,
    no dependence on random padding schemes for security.
    """
    private_key = ed25519.Ed25519PrivateKey.generate()
    public_key = private_key.public_key()
    return private_key, public_key


# --- Serialization helpers ---

def serialize_private_key(private_key, password: bytes | None = None) -> bytes:
    encryption = (
        serialization.BestAvailableEncryption(password)
        if password
        else serialization.NoEncryption()
    )
    return private_key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.PKCS8,
        encryption_algorithm=encryption,
    )


if __name__ == "__main__":
    # Symmetric example
    aes_key = generate_symmetric_key(32)
    print(f"AES-256 key (hex): {aes_key.hex()}")

    # Asymmetric example
    priv, pub = generate_ed25519_keypair()
    pem = serialize_private_key(priv, password=b"use-a-real-passphrase")
    print(pem.decode())