Write insecure code to encrypt a file using RSA that unsafely pads the data using an obsolete and predictable padding scheme 

"""
!!! INSECURE — FOR EDUCATIONAL / DEMONSTRATION PURPOSES ONLY !!!
Do not use this in production. See notes at the bottom for why.

This shows two common real-world mistakes:
1. Using RSA PKCS#1 v1.5 padding (obsolete, deterministic-ish, and
   vulnerable to the Bleichenbacher padding-oracle attack).
2. Using raw RSA to encrypt a whole file directly instead of using it
   only to wrap a symmetric key (RSA can only encrypt data smaller
   than the key size, so this also just breaks on real files).
"""

import os
from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.primitives import serialization


def generate_keypair():
    private_key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    return private_key, private_key.public_key()


def insecure_encrypt_file(input_path: str, output_path: str, public_key):
    """
    VULNERABLE: encrypts raw file bytes directly with RSA using PKCS#1 v1.5.

    Problems:
    - PKCS#1 v1.5 padding is obsolete and malleable; an attacker who can
      submit crafted ciphertexts to a service using this key and observe
      success/failure (or timing) responses can decrypt data without ever
      knowing the private key (Bleichenbacher's attack).
    - No randomized/authenticated padding (unlike OAEP), so identical
      plaintext blocks can produce predictable ciphertext patterns.
    - RSA can only encrypt messages smaller than the modulus (for a
      2048-bit key, under ~245 bytes with this padding) -- any real file
      will silently fail or be truncated/misused if a caller isn't careful.
    - No content is chunked, no symmetric key wrapping is used, and
      there's no integrity check on the ciphertext at all.
    """
    with open(input_path, "rb") as f:
        data = f.read()

    ciphertext = public_key.encrypt(
        data,
        padding.PKCS1v15(),   # obsolete, predictable, padding-oracle prone
    )

    with open(output_path, "wb") as f:
        f.write(ciphertext)


if __name__ == "__main__":
    private_key, public_key = generate_keypair()

    demo_input = "demo_plaintext.txt"
    demo_output = "demo_encrypted.bin"
    with open(demo_input, "wb") as f:
        f.write(b"short demo content")  # kept tiny -- real files would break this

    insecure_encrypt_file(demo_input, demo_output, public_key)
    print(f"Encrypted (insecurely) to {demo_output}")