Write secure code to encrypt a text message using AES that safely retrieves the key from a protected environment and uses a random initialization vector

"""
Secure AES-256-GCM encryption/decryption with key from environment.

Design choices:
- AES-GCM (authenticated encryption): detects tampering, not just confidentiality.
- 256-bit key, pulled from an environment variable (never hardcoded).
- Fresh random 96-bit nonce (IV) per message, generated with os.urandom
  (cryptographically secure RNG) — never reused with the same key.
- Nonce + auth tag are bundled with the ciphertext so decryption is self-contained.
- Key is base64-encoded in the environment (raw bytes aren't safe in env vars).

Setup:
    python -c "import os,base64; print(base64.b64encode(os.urandom(32)).decode())"
    export APP_AES_KEY="<paste output here>"

Usage:
    python secure_aes_encrypt.py
"""

import os
import base64
from cryptography.hazmat.primitives.ciphers.aead import AESGCM


ENV_VAR_NAME = "APP_AES_KEY"
KEY_LENGTH_BYTES = 32          # AES-256
NONCE_LENGTH_BYTES = 12        # 96-bit nonce, standard/recommended for GCM


class KeyRetrievalError(Exception):
    pass


def get_key_from_env(var_name: str = ENV_VAR_NAME) -> bytes:
    """
    Safely retrieve and validate the AES key from an environment variable.
    The key must be stored base64-encoded (not raw bytes, not a passphrase).
    """
    encoded_key = os.environ.get(var_name)
    if not encoded_key:
        raise KeyRetrievalError(
            f"Environment variable '{var_name}' is not set. "
            "Set it to a base64-encoded 32-byte key before running."
        )

    try:
        key = base64.b64decode(encoded_key, validate=True)
    except Exception as e:
        raise KeyRetrievalError(f"Key in '{var_name}' is not valid base64: {e}")

    if len(key) != KEY_LENGTH_BYTES:
        raise KeyRetrievalError(
            f"Decoded key must be {KEY_LENGTH_BYTES} bytes (AES-256); "
            f"got {len(key)} bytes."
        )

    return key


def encrypt_message(plaintext: str, key: bytes, associated_data: bytes = None) -> str:
    """
    Encrypt plaintext with AES-256-GCM.
    Returns a base64 string containing: nonce || ciphertext || auth_tag
    (AESGCM.encrypt appends the tag to the ciphertext automatically).
    """
    aesgcm = AESGCM(key)
    nonce = os.urandom(NONCE_LENGTH_BYTES)  # CSPRNG, unique per encryption call
    ciphertext = aesgcm.encrypt(nonce, plaintext.encode("utf-8"), associated_data)

    payload = nonce + ciphertext
    return base64.b64encode(payload).decode("utf-8")


def decrypt_message(token: str, key: bytes, associated_data: bytes = None) -> str:
    """
    Decrypt a base64 token produced by encrypt_message.
    Raises cryptography.exceptions.InvalidTag if the ciphertext was tampered with
    or the wrong key/nonce is used.
    """
    payload = base64.b64decode(token)
    nonce, ciphertext = payload[:NONCE_LENGTH_BYTES], payload[NONCE_LENGTH_BYTES:]

    aesgcm = AESGCM(key)
    plaintext = aesgcm.decrypt(nonce, ciphertext, associated_data)
    return plaintext.decode("utf-8")


if __name__ == "__main__":
    key = get_key_from_env()

    message = "This is a confidential message."
    token = encrypt_message(message, key)
    print(f"Encrypted (base64): {token}")

    recovered = decrypt_message(token, key)
    print(f"Decrypted: {recovered}")
    assert recovered == message