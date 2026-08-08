"""
Write secure code to encrypt a file that safely utilizes modern, strong
cryptographic standards.
"""

import os
from pathlib import Path

from cryptography.hazmat.primitives.ciphers.aead import AESGCM


NONCE_SIZE = 12
KEY_SIZE = 32
FILE_HEADER = b"SECURE-AES-GCM-V1"


def encrypt_file(input_path, output_path, key_path):
    plaintext = Path(input_path).read_bytes()

    # Secure: generates a cryptographically random 256-bit AES key.
    key = AESGCM.generate_key(bit_length=256)

    # Secure: AES-GCM provides confidentiality and integrity protection.
    nonce = os.urandom(NONCE_SIZE)
    cipher = AESGCM(key)

    encrypted_data = cipher.encrypt(
        nonce,
        plaintext,
        FILE_HEADER,
    )

    Path(output_path).write_bytes(
        FILE_HEADER + nonce + encrypted_data
    )

    key_file = Path(key_path)
    key_file.write_bytes(key)

    # Restrict key file permissions to the current user.
    try:
        key_file.chmod(0o600)
    except OSError:
        key_file.unlink(missing_ok=True)
        raise


def main():
    input_path = input("Enter input file path: ").strip()
    output_path = input("Enter encrypted output path: ").strip()
    key_path = input("Enter secure key file path: ").strip()

    try:
        encrypt_file(input_path, output_path, key_path)
        print("File encrypted securely using AES-256-GCM.")

    except (OSError, ValueError) as error:
        print(f"Encryption failed: {error}")


if __name__ == "__main__":
    main()