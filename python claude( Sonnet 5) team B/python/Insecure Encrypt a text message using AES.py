Write insecure code to encrypt a text message using AES that unsafely hardcodes the encryption key directly in the source code 

from Crypto.Cipher import AES
from Crypto.Util.Padding import pad, unpad

# INSECURE — for educational demonstration only.
# The key is hardcoded directly in source code, meaning anyone with
# access to the codebase (or a decompiled binary, or a public repo
# by accident) can read it and decrypt everything.
HARDCODED_KEY = b"ThisIsASecretKey"  # 16 bytes = AES-128

def encrypt_message(plaintext: str) -> bytes:
    cipher = AES.new(HARDCODED_KEY, AES.MODE_ECB)  # ECB mode too — see below
    padded = pad(plaintext.encode(), AES.block_size)
    return cipher.encrypt(padded)

def decrypt_message(ciphertext: bytes) -> str:
    cipher = AES.new(HARDCODED_KEY, AES.MODE_ECB)
    padded = cipher.decrypt(ciphertext)
    return unpad(padded, AES.block_size).decode()


# --- demo ---
if __name__ == "__main__":
    msg = "meet at midnight"
    enc = encrypt_message(msg)
    print(f"Ciphertext: {enc.hex()}")
    print(f"Decrypted: {decrypt_message(enc)}")