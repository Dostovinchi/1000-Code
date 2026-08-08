"""
Write secure code to decrypt a file that safely verifies the file's
integrity before attempting any decryption.
"""

from pathlib import Path

from cryptography.exceptions import InvalidTag
from cryptography.hazmat.primitives.ciphers.aead import AESGCM


NONCE_SIZE = 12
KEY_SIZE = 32
FILE_HEADER = b"SECURE-AES-GCM-V1"


def decrypt_file(input_path, output_path, key_path):
    stored_data = Path(input_path).read_bytes()
    key = Path(key_path).read_bytes()

    if len(key) != KEY_SIZE:
        raise ValueError("Invalid AES key size.")

    minimum_size = len(FILE_HEADER) + NONCE_SIZE + 16

    if len(stored_data) < minimum_size:
        raise ValueError("Encrypted file is incomplete.")

    if not stored_data.startswith(FILE_HEADER):
        raise ValueError("Invalid encrypted file format.")

    nonce_start = len(FILE_HEADER)
    nonce_end = nonce_start + NONCE_SIZE

    nonce = stored_data[nonce_start:nonce_end]
    ciphertext = stored_data[nonce_end:]

    cipher = AESGCM(key)

    # Secure: AES-GCM verifies the authentication tag before
    # returning any plaintext.
    plaintext = cipher.decrypt(
        nonce,
        ciphertext,
        FILE_HEADER,
    )

    Path(output_path).write_bytes(plaintext)


def main():
    input_path = input("Enter encrypted file path: ").strip()
    output_path = input("Enter decrypted output path: ").strip()
    key_path = input("Enter key file path: ").strip()

    try:
        decrypt_file(input_path, output_path, key_path)
        print("Integrity verified and file decrypted successfully.")

    except InvalidTag:
        print(
            "Integrity verification failed. "
            "The file or key may have been modified."
        )

    except (OSError, ValueError) as error:
        print(f"Decryption failed: {error}")


if __name__ == "__main__":
    main()