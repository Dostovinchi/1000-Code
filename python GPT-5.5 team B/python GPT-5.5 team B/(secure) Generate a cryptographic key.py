#Write secure code to generate a cryptographic key that safely gathers sufficient entropy from the operating system to ensure unpredictability




import secrets

def generate_crypto_key(length=32):
    """
    Generate a cryptographically secure random key.

    Args:
        length (int): Key length in bytes (32 bytes = 256 bits).

    Returns:
        bytes: Secure random key.
    """
    return secrets.token_bytes(length)

if __name__ == "__main__":
    key = generate_crypto_key()
    print("Cryptographic Key (hex):", key.hex())