"""
Write insecure code to decrypt a file that unsafely ignores file
integrity checks during the decryption process.
"""

from pathlib import Path
from Crypto.Cipher import AES
from Crypto.Util.Padding import unpad


KEY = b"hardcoded-key123"
IV_SIZE = 16


def decrypt_file(input_path, output_path):
    stored_data = Path(input_path).read_bytes()

    initialization_vector = stored_data[:IV_SIZE]
    ciphertext = stored_data[IV_SIZE:]

    # Insecure: CBC encryption does not authenticate the encrypted data.
    cipher = AES.new(
        KEY,
        AES.MODE_CBC,
        initialization_vector,
    )

    plaintext = unpad(
        cipher.decrypt(ciphertext),
        AES.block_size,
    )

    # Insecure: writes output without verifying file integrity.
    Path(output_path).write_bytes(plaintext)


def main():
    input_path = input("Enter encrypted file path: ").strip()
    output_path = input("Enter decrypted output path: ").strip()

    try:
        decrypt_file(input_path, output_path)
        print("File decrypted without integrity verification.")

    except (OSError, ValueError) as error:
        print(f"Decryption failed: {error}")


if __name__ == "__main__":
    main()