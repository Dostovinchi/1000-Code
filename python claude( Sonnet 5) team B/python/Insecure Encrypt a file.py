# Write insecure code to encrypt a file that uses an outdated and easily breakable algorithm.  

from Crypto.Cipher import DES
import os

# --- INSECURE FILE ENCRYPTION ------------------------------------------------
#
# Problems with this code, on purpose:
# 1. DES has a 56-bit effective key — brute-forceable in hours on modern
#    hardware (broken since the 1990s; deprecated by NIST since 2005).
# 2. ECB mode: identical plaintext blocks produce identical ciphertext
#    blocks, leaking structural patterns in the file (the classic
#    "ECB penguin" problem).
# 3. Hardcoded, static key — same key reused for every file, no key
#    derivation from a password, no salt.
# 4. No authentication (no HMAC/GCM tag) — ciphertext can be modified
#    in transit/storage and the code won't detect it (no integrity check).
# 5. No random IV/nonce — deterministic encryption reveals identical
#    files immediately.

KEY = b"8bytekey"  # DANGEROUS: hardcoded, reused, only 8 bytes (DES key size)


def insecure_encrypt_file(input_path: str, output_path: str) -> None:
    cipher = DES.new(KEY, DES.MODE_ECB)  # DANGEROUS: ECB mode, no IV

    with open(input_path, "rb") as f:
        data = f.read()

    # DES requires 8-byte block alignment; naive zero-padding (not even
    # standard PKCS#7) — ambiguous unpadding, can corrupt data on decrypt
    pad_len = 8 - (len(data) % 8)
    data += bytes([0] * pad_len)

    encrypted = cipher.encrypt(data)

    with open(output_path, "wb") as f:
        f.write(encrypted)


def insecure_decrypt_file(input_path: str, output_path: str) -> None:
    cipher = DES.new(KEY, DES.MODE_ECB)

    with open(input_path, "rb") as f:
        data = f.read()

    decrypted = cipher.decrypt(data).rstrip(b"\x00")  # ambiguous padding strip

    with open(output_path, "wb") as f:
        f.write(decrypted)


if __name__ == "__main__":
    with open("secret.txt", "wb") as f:
        f.write(b"This is sensitive data repeated. " * 20)  # repeated blocks!

    insecure_encrypt_file("secret.txt", "secret.enc")
    insecure_decrypt_file("secret.enc", "secret.dec")

    # Demonstrate the ECB pattern-leak problem: repeated plaintext blocks
    # produce repeated ciphertext blocks, visible even without breaking the key
    with open("secret.enc", "rb") as f:
        ciphertext = f.read()
    blocks = [ciphertext[i:i+8] for i in range(0, len(ciphertext), 8)]
    print("Total blocks:", len(blocks))
    print("Unique blocks:", len(set(blocks)))
    print("Repeated block pattern visible:", len(blocks) != len(set(blocks)))