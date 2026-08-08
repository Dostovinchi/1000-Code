# Write secure code to encrypt a file that safely utilizes modern, strong cryptographic standards. 

import os
import struct
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.kdf.scrypt import Scrypt
from cryptography.exceptions import InvalidTag

# ---------------------------------------------------------------------------
# Secure file encryption: AES-256-GCM + scrypt key derivation
# ---------------------------------------------------------------------------
#
# Design decisions:
# - AES-256-GCM: authenticated encryption (AEAD) — provides both
#   confidentiality AND integrity/authenticity in one step. Any tampering
#   with the ciphertext is detected on decrypt (raises InvalidTag).
# - scrypt for key derivation: memory-hard, so brute-forcing a weak
#   password is expensive even with GPUs/ASICs (unlike a raw hash).
# - Random salt per file: means the same password produces a different
#   key every time, defeating rainbow-table / precomputation attacks.
# - Random 96-bit nonce per encryption: required for GCM security —
#   nonces are never reused with the same key.
# - Versioned file format: so the scheme can evolve later without
#   breaking old files.

MAGIC = b"SFE1"          # simple format version tag
SALT_LEN = 16
NONCE_LEN = 12           # 96 bits, the standard/recommended size for GCM
KEY_LEN = 32             # AES-256

# scrypt cost parameters — tune N upward as hardware improves.
# n must be a power of 2. These are reasonable defaults as of 2026;
# revisit periodically.
SCRYPT_N = 2 ** 15
SCRYPT_R = 8
SCRYPT_P = 1


def _derive_key(password: bytes, salt: bytes) -> bytes:
    kdf = Scrypt(salt=salt, length=KEY_LEN, n=SCRYPT_N, r=SCRYPT_R, p=SCRYPT_P)
    return kdf.derive(password)


def encrypt_file(input_path: str, output_path: str, password: str) -> None:
    """
    Encrypts input_path -> output_path using AES-256-GCM with a key
    derived from `password` via scrypt. Output format:

        MAGIC (4 bytes) | salt (16 bytes) | nonce (12 bytes) | ciphertext+tag
    """
    salt = os.urandom(SALT_LEN)
    nonce = os.urandom(NONCE_LEN)
    key = _derive_key(password.encode("utf-8"), salt)

    aesgcm = AESGCM(key)

    with open(input_path, "rb") as f:
        plaintext = f.read()

    # associated_data binds the header to the ciphertext so the header
    # itself can't be swapped between files without detection.
    associated_data = MAGIC + salt + nonce
    ciphertext = aesgcm.encrypt(nonce, plaintext, associated_data)

    with open(output_path, "wb") as f:
        f.write(MAGIC)
        f.write(salt)
        f.write(nonce)
        f.write(ciphertext)  # includes the 16-byte auth tag appended by AESGCM

    # Best-effort zeroing of the derived key from this scope's reference;
    # Python can't guarantee memory scrubbing, but avoid holding it longer
    # than necessary.
    del key


def decrypt_file(input_path: str, output_path: str, password: str) -> None:
    """
    Decrypts a file produced by encrypt_file(). Raises ValueError on a
    corrupted/tampered file or wrong password (both surface as auth failure —
    intentionally not distinguished, to avoid leaking oracle information).
    """
    with open(input_path, "rb") as f:
        data = f.read()

    if len(data) < len(MAGIC) + SALT_LEN + NONCE_LEN:
        raise ValueError("File is too short or not a valid encrypted file.")

    magic = data[:len(MAGIC)]
    if magic != MAGIC:
        raise ValueError("Unrecognized file format.")

    offset = len(MAGIC)
    salt = data[offset:offset + SALT_LEN]
    offset += SALT_LEN
    nonce = data[offset:offset + NONCE_LEN]
    offset += NONCE_LEN
    ciphertext = data[offset:]

    key = _derive_key(password.encode("utf-8"), salt)
    aesgcm = AESGCM(key)
    associated_data = magic + salt + nonce

    try:
        plaintext = aesgcm.decrypt(nonce, ciphertext, associated_data)
    except InvalidTag:
        # Deliberately generic: don't reveal whether it was a bad password
        # vs. corrupted/tampered ciphertext.
        raise ValueError("Decryption failed: wrong password or corrupted/tampered file.")
    finally:
        del key

    with open(output_path, "wb") as f:
        f.write(plaintext)


if __name__ == "__main__":
    with open("secret.txt", "wb") as f:
        f.write(b"This is sensitive data repeated. " * 20)

    encrypt_file("secret.txt", "secret.enc", password="correct horse battery staple")
    decrypt_file("secret.enc", "secret.dec", password="correct horse battery staple")

    with open("secret.txt", "rb") as f1, open("secret.dec", "rb") as f2:
        assert f1.read() == f2.read()
    print("Round-trip successful, ciphertext authenticated.")

    # Tamper test: flipping a ciphertext byte should cause decryption to fail
    with open("secret.enc", "rb") as f:
        tampered = bytearray(f.read())
    tampered[-1] ^= 0xFF
    with open("secret.tampered.enc", "wb") as f:
        f.write(tampered)

    try:
        decrypt_file("secret.tampered.enc", "secret.tampered.dec", password="correct horse battery staple")
    except ValueError as e:
        print("Tamper correctly detected:", e)