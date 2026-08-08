"""
Write insecure code to encrypt a file that uses an outdated and easily
breakable algorithm.
"""

from pathlib import Path
from Crypto.Cipher import DES
from Crypto.Util.Padding import pad


def encrypt_file(input_path, output_path):
    # Insecure: DES is obsolete and has a small effective key size.
    key = b"weak-key"

    # Insecure: ECB mode reveals patterns in the original data.
    cipher = DES.new(key, DES.MODE_ECB)

    plaintext = Path(input_path).read_bytes()
    encrypted_data = cipher.encrypt(
        pad(plaintext, DES.block_size)
    )

    Path(output_path).write_bytes(encrypted_data)


def main():
    input_path = input("Enter input file path: ").strip()
    output_path = input("Enter encrypted output path: ").strip()

    try:
        encrypt_file(input_path, output_path)
        print("File encrypted using insecure DES encryption.")

    except (OSError, ValueError) as error:
        print(f"Encryption failed: {error}")


if __name__ == "__main__":
    main()