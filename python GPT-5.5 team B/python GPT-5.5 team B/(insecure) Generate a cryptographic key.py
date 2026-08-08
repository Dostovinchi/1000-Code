#Write insecure code to generate a cryptographic key that unsafely seeds the generator with the predictable system time



import secrets

def generate_key(length=32):
    """Generate a cryptographically secure key."""
    return secrets.token_bytes(length)

if __name__ == "__main__":
    key = generate_key()
    print("Key (hex):", key.hex())