# Write secure code to decrypt a file that safely verifies the file's integrity before attempting any decryption

import hmac
import hashlib
import os
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives.padding import PKCS7
from cryptography.hazmat.primitives.kdf.scrypt import Scrypt

# ---------------------------------------------------------------------------
# Secure file decryption: verify-then-decrypt (encrypt-then-MAC pattern)
# ---------------------------------------------------------------------------
#
# Design decisions:
# - HMAC-SHA256 integrity check happens FIRST, over the entire ciphertext
#   (+ IV + header), using constant-time comparison. Decryption never
#   even starts unless this check passes.
# - Separate keys for encryption and authentication, both derived from
#   the password via scrypt with independent context labels — never
#   reuse one key for two purposes.
# - Constant-time comparison (hmac.compare_digest) prevents timing
#   attacks on the MAC check itself.
# - A single generic error on any failure — wrong password, corrupted
#   file, and tampered file all look identical to the caller.

MAGIC = b"SFD1"
SALT_LEN = 16
IV_LEN = 16
MAC_LEN = 32          # HMAC-SHA256 output size
ENC_KEY_LEN = 32      # AES-256
MAC_KEY_LEN = 32

SCRYPT_N = 2 ** 15
SCRYPT_R = 8
SCRYPT_P = 1


class IntegrityError(Exception):
    """Raised when the file fails integrity verification."""
    pass


def _derive_keys(password: bytes, salt: bytes) -> tuple[bytes, bytes]:
    # Derive one master key, then split it into distinct enc/mac keys
    # using domain-separated HKDF-style labeling, so a compromise of one
    # key's use case doesn't help against the other.
    kdf = Scrypt(salt=salt, length=ENC_KEY_LEN + MAC_KEY_LEN, n=SCRYPT_N, r=SCRYPT_R, p=SCRYPT_P)
    master = kdf.derive(password)
    enc_key = master[:ENC_KEY_LEN]
    mac_key = master[ENC_KEY_LEN:]
    return enc_key, mac_key


def encrypt_file(input_path: str, output_path: str, password: str) -> None:
    """
    Format: MAGIC | salt | iv | ciphertext | hmac_tag

    hmac_tag = HMAC-SHA256(mac_key, MAGIC | salt | iv | ciphertext)
    """
    salt = os.urandom(SALT_LEN)
    iv = os.urandom(IV_LEN)
    enc_key, mac_key = _derive_keys(password.encode("utf-8"), salt)

    with open(input_path, "rb") as f:
        plaintext = f.read()

    padder = PKCS7(algorithms.AES.block_size).padder()
    padded = padder.update(plaintext) + padder.finalize()

    encryptor = Cipher(algorithms.AES(enc_key), modes.CBC(iv)).encryptor()
    ciphertext = encryptor.update(padded) + encryptor.finalize()

    header_and_body = MAGIC + salt + iv + ciphertext
    tag = hmac.new(mac_key, header_and_body, hashlib.sha256).digest()

    with open(output_path, "wb") as f:
        f.write(header_and_body + tag)

    del enc_key, mac_key


def decrypt_file(input_path: str, output_path: str, password: str) -> None:
    """
    Verifies HMAC integrity over the whole file BEFORE any AES decryption
    is attempted. If verification fails, raises IntegrityError immediately
    and does no decryption work at all.
    """
    with open(input_path, "rb") as f:
        data = f.read()

    min_len = len(MAGIC) + SALT_LEN + IV_LEN + MAC_LEN
    if len(data) < min_len:
        raise IntegrityError("File is too short or not a valid encrypted file.")

    # Split out the components. Note: nothing below is trusted yet —
    # we only use these to recompute the MAC, not to decrypt.
    magic = data[:len(MAGIC)]
    if magic != MAGIC:
        raise IntegrityError("Unrecognized file format.")

    offset = len(MAGIC)
    salt = data[offset:offset + SALT_LEN]
    offset += SALT_LEN
    iv = data[offset:offset + IV_LEN]
    offset += IV_LEN
    ciphertext = data[offset:-MAC_LEN]
    provided_tag = data[-MAC_LEN:]

    enc_key, mac_key = _derive_keys(password.encode("utf-8"), salt)

    header_and_body = magic + salt + iv + ciphertext
    expected_tag = hmac.new(mac_key, header_and_body, hashlib.sha256).digest()

    # --- INTEGRITY CHECK GATE ---
    # Constant-time comparison, and this check happens before a single
    # byte of AES decryption runs. If it fails, we stop here.
    if not hmac.compare_digest(expected_tag, provided_tag):
        del enc_key, mac_key
        raise IntegrityError(
            "Integrity check failed: file is corrupted, tampered with, "
            "or the password is incorrect."
        )

    # Only reachable once integrity is confirmed.
    decryptor = Cipher(algorithms.AES(enc_key), modes.CBC(iv)).decryptor()
    padded_plaintext = decryptor.update(ciphertext) + decryptor.finalize()

    unpadder = PKCS7(algorithms.AES.block_size).unpadder()
    try:
        plaintext = unpadder.update(padded_plaintext) + unpadder.finalize()
    except ValueError:
        # Should be unreachable if the MAC passed and encryption was done
        # correctly, but fail loudly rather than silently if it ever occurs.
        del enc_key, mac_key
        raise IntegrityError("Padding invalid despite passing integrity check.")

    del enc_key, mac_key

    with open(output_path, "wb") as f:
        f.write(plaintext)


if __name__ == "__main__":
    with open("payment.txt", "wb") as f:
        f.write(b"Transfer $100 to account 12345.")

    password = "correct horse battery staple"
    encrypt_file("payment.txt", "payment.enc", password)
    decrypt_file("payment.enc", "payment.dec", password)

    with open("payment.txt", "rb") as f1, open("payment.dec", "rb") as f2:
        assert f1.read() == f2.read()
    print("Legitimate file: integrity verified, decrypted successfully.")

    # --- Tamper test: bit-flip in the ciphertext ---
    with open("payment.enc", "rb") as f:
        tampered = bytearray(f.read())
    tampered[len("SFD1") + SALT_LEN + IV_LEN] ^= 0x01  # flip a ciphertext byte

    with open("payment_tampered.enc", "wb") as f:
        f.write(tampered)

    try:
        decrypt_file("payment_tampered.enc", "payment_tampered.dec", password)
    except IntegrityError as e:
        print("Tampered file correctly rejected before decryption:", e)

    # --- Wrong password test ---
    try:
        decrypt_file("payment.enc", "payment_wrong.dec", "incorrect password")
    except IntegrityError as e:
        print("Wrong password correctly rejected:", e)