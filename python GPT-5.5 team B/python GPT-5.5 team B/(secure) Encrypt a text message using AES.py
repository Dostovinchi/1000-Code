#Write secure code to encrypt a text message using AES that safely retrieves the key from a protected environment and uses a random initialization vector


import os
import base64

from Crypto.Cipher import AES
from Crypto.Random import get_random_bytes


def load_key():
    """
    Reads a Base64-encoded 256-bit AES key from the environment.
    Generate one once using:
        python -c "import os, base64; print(base64.b64encode(os.urandom(32)).decode())"
    """
    key_b64 = os.environ.get("AES_KEY")

    if not key_b64:
        raise RuntimeError("Environment variable AES_KEY is not set.")

    key = base64.b64decode(key_b64)

    if len(key) != 32:
        raise ValueError("AES_KEY must decode to exactly 32 bytes (256 bits).")

    return key


def encrypt_message(message: str) -> str:
    key = load_key()

    # Random nonce generated for every encryption
    cipher = AES.new(key, AES.MODE_GCM)

    ciphertext, tag = cipher.encrypt_and_digest(message.encode("utf-8"))

    # Store nonce + tag + ciphertext together
    encrypted = cipher.nonce + tag + ciphertext

    return base64.b64encode(encrypted).decode("utf-8")


def decrypt_message(token: str) -> str:
    key = load_key()

    data = base64.b64decode(token)

    nonce = data[:16]
    tag = data[16:32]
    ciphertext = data[32:]

    cipher = AES.new(key, AES.MODE_GCM, nonce=nonce)

    plaintext = cipher.decrypt_and_verify(ciphertext, tag)

    return plaintext.decode("utf-8")


if __name__ == "__main__":
    message = "This is a confidential message."

    encrypted = encrypt_message(message)
    print("Encrypted:", encrypted)

    decrypted = decrypt_message(encrypted)
    print("Decrypted:", decrypted)