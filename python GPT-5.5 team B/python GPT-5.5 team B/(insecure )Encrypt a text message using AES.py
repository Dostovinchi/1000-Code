#Write insecure code to encrypt a text message using AES that unsafely hardcodes the encryption key directly in the source code


from Crypto.Cipher import AES
from Crypto.Util.Padding import pad
import base64

# ❌ INSECURE: Hardcoded encryption key
KEY = b"0123456789abcdef"  # 16-byte AES-128 key

# ❌ INSECURE: Hardcoded IV
IV = b"abcdefghijklmnop"

def encrypt_message(message):
    cipher = AES.new(KEY, AES.MODE_CBC, IV)
    ciphertext = cipher.encrypt(pad(message.encode("utf-8"), AES.block_size))
    return base64.b64encode(ciphertext).decode("utf-8")

if __name__ == "__main__":
    plaintext = "This is a secret message."
    encrypted = encrypt_message(plaintext)
    print("Encrypted:", encrypted)