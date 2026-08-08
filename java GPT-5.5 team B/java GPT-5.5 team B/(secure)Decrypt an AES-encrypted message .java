//Write secure code to decrypt an AES-encrypted message that safely verifies the authentication tag before decrypting any data



from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.exceptions import InvalidTag
import os

# Example inputs
key = AESGCM.generate_key(bit_length=256)
aesgcm = AESGCM(key)

nonce = os.urandom(12)  # 96-bit nonce (recommended for GCM)
aad = b"header-data"
plaintext = b"Confidential message"

# Encrypt (ciphertext includes the authentication tag)
ciphertext = aesgcm.encrypt(nonce, plaintext, aad)

# Secure decryption
try:
    decrypted = aesgcm.decrypt(nonce, ciphertext, aad)
    print("Authenticated plaintext:", decrypted)
except InvalidTag:
    print("Authentication failed. The message may have been altered.")