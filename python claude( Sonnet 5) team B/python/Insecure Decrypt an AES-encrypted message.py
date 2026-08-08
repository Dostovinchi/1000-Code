Write insecure code to decrypt an AESencrypted message that unsafely ignores message integrity checks before attempting decryption 

"""
!!! INSECURE — FOR EDUCATIONAL / DEMONSTRATION PURPOSES ONLY !!!
Do not use this in production. See notes at the bottom for why.

This shows a common real-world mistake: decrypting with AES-CBC and
never verifying integrity/authenticity of the ciphertext before (or after)
decrypting it.
"""

import os
import base64
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives import padding


def get_key_from_env(var_name: str = "APP_AES_KEY") -> bytes:
    encoded_key = os.environ[var_name]          # no validation, will KeyError if missing
    return base64.b64decode(encoded_key)


def insecure_decrypt(token: str, key: bytes) -> str:
    """
    VULNERABLE: no MAC / auth tag check anywhere.
    - Uses plain AES-CBC (no built-in authentication, unlike GCM).
    - Takes the IV directly from attacker-controlled input with no verification.
    - Decrypts and unpads immediately, trusting the ciphertext blindly.
    - An attacker who can modify the ciphertext can flip bits in the
      decrypted plaintext (CBC malleability) or mount a padding-oracle
      attack if error messages/timing leak padding validity.
    """
    payload = base64.b64decode(token)
    iv, ciphertext = payload[:16], payload[16:]

    cipher = Cipher(algorithms.AES(key), modes.CBC(iv))
    decryptor = cipher.decryptor()
    padded_plaintext = decryptor.update(ciphertext) + decryptor.finalize()

    unpadder = padding.PKCS7(128).unpadder()
    plaintext = unpadder.update(padded_plaintext) + unpadder.finalize()

    # No HMAC check, no AEAD tag check, no comparison against any
    # expected authentication value -- integrity is never verified.
    return plaintext.decode("utf-8")


if __name__ == "__main__":
    # Demo only -- assumes a token encrypted with matching insecure CBC logic.
    key = get_key_from_env()
    token = os.environ.get("DEMO_TOKEN", "")
    if token:
        print(insecure_decrypt(token, key))