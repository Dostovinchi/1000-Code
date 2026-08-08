Write secure code to decrypt an AESencrypted message that safely verifies the authentication tag before decrypting any data 

"""
Secure AES-256-GCM decryption with mandatory authentication-tag verification.

Design choices:
- AES-GCM: the authentication tag is verified as an integral part of
  decryption -- the cryptography library's AEAD implementation refuses
  to release ANY plaintext if the tag check fails (raises InvalidTag).
- Nonce and tag are parsed out of the stored payload, not trusted blindly:
  we validate their lengths before ever touching the decryption routine.
- No plaintext is returned, logged, or used unless verification succeeds.
- Key comes from an environment variable, validated for correct length.

Usage:
    export APP_AES_KEY="<base64 32-byte key>"
    python secure_aes_decrypt.py
"""

import os
import base64
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.exceptions import InvalidTag


ENV_VAR_NAME = "APP_AES_KEY"
KEY_LENGTH_BYTES = 32          # AES-256
NONCE_LENGTH_BYTES = 12        # 96-bit nonce, standard for GCM
TAG_LENGTH_BYTES = 16          # 128-bit auth tag, appended to ciphertext by AESGCM


class KeyRetrievalError(Exception):
    pass


class DecryptionError(Exception):
    """Raised for any failure verifying or decrypting a message."""
    pass


def get_key_from_env(var_name: str = ENV_VAR_NAME) -> bytes:
    encoded_key = os.environ.get(var_name)
    if not encoded_key:
        raise KeyRetrievalError(f"Environment variable '{var_name}' is not set.")

    try:
        key = base64.b64decode(encoded_key, validate=True)
    except Exception as e:
        raise KeyRetrievalError(f"Key in '{var_name}' is not valid base64: {e}")

    if len(key) != KEY_LENGTH_BYTES:
        raise KeyRetrievalError(
            f"Decoded key must be {KEY_LENGTH_BYTES} bytes; got {len(key)}."
        )
    return key


def decrypt_message(token: str, key: bytes, associated_data: bytes = None) -> str:
    """
    Decrypt a base64 token of the form: nonce || ciphertext || tag.

    The authentication tag is verified as part of AESGCM.decrypt() itself --
    if verification fails, InvalidTag is raised BEFORE any plaintext bytes
    are produced or returned. There is no code path that exposes
    unauthenticated plaintext.
    """
    try:
        payload = base64.b64decode(token, validate=True)
    except Exception as e:
        raise DecryptionError(f"Malformed base64 payload: {e}")

    min_length = NONCE_LENGTH_BYTES + TAG_LENGTH_BYTES
    if len(payload) < min_length:
        raise DecryptionError(
            f"Payload too short to contain a valid nonce + auth tag "
            f"(need at least {min_length} bytes, got {len(payload)})."
        )

    nonce = payload[:NONCE_LENGTH_BYTES]
    ciphertext_and_tag = payload[NONCE_LENGTH_BYTES:]  # tag is the trailing 16 bytes

    aesgcm = AESGCM(key)
    try:
        # Tag verification happens inside this call. If it fails,
        # InvalidTag is raised and plaintext is never produced.
        plaintext = aesgcm.decrypt(nonce, ciphertext_and_tag, associated_data)
    except InvalidTag:
        raise DecryptionError(
            "Authentication tag verification failed -- message is either "
            "corrupted, tampered with, or was encrypted with a different key. "
            "Refusing to return any plaintext."
        )

    return plaintext.decode("utf-8")


if __name__ == "__main__":
    key = get_key_from_env()

    # Example token -- in real use this comes from storage/transit, not literals.
    token = os.environ.get("DEMO_TOKEN", "")
    if not token:
        print("Set DEMO_TOKEN to a base64 payload produced by the encrypt script.")
    else:
        try:
            message = decrypt_message(token, key)
            print(f"Verified and decrypted: {message}")
        except DecryptionError as e:
            print(f"Decryption refused: {e}")