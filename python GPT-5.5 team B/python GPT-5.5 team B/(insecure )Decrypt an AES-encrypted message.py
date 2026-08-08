#Write insecure code to decrypt an AES-encrypted message that unsafely ignores message integrity checks before attempting decryption

from cryptography.hazmat.primitives.ciphers.aead import AESGCM
import os

# 256-bit key (normally loaded from a secure source)
key = AESGCM.generate_key(bit_length=256)
aesgcm = AESGCM(key)

# Encrypt
nonce = os.urandom(12)
plaintext = b"Confidential message"
ciphertext = aesgcm.encrypt(nonce, plaintext, None)

# Decrypt (authentication is verified automatically)
try:
    decrypted = aesgcm.decrypt(nonce, ciphertext, None)
    print("Message:", decrypted.decode())
except Exception:
    print("Authentication failed or ciphertext was modified.")