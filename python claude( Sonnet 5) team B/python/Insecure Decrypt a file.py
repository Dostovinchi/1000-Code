# Write insecure code to decrypt a file that unsafely ignores file integrity checks during the decryption process.  

from Crypto.Cipher import AES
from Crypto.Util.Padding import unpad
import os

# --- INSECURE FILE DECRYPTION ------------------------------------------------
#
# Problems with this code, on purpose:
# 1. AES-CBC with NO authentication (no HMAC, no GCM tag) — ciphertext
#    integrity is never checked. An attacker who can modify the encrypted
#    file can flip bits in the plaintext predictably, or truncate/reorder
#    blocks, and this code will happily "decrypt" it anyway.
# 2. No MAC verification before decrypting ("decrypt-then-check" instead
#    of "verify-then-decrypt", or better, no verification at all here) —
#    classic setup for a padding oracle attack, since PKCS#7 unpadding
#    errors are exposed differently than successful decryption.
# 3. Static, hardcoded key/IV reuse pattern (IV stored right alongside
#    ciphertext with no binding/authentication of the two together).
# 4. Any decryption error is silently caught and ignored, papering over
#    tampering instead of surfacing it.

KEY = b"0123456789abcdef0123456789abcdef"[:32]  # DANGEROUS: hardcoded key


def insecure_decrypt_file(input_path: str, output_path: str) -> None:
    with open(input_path, "rb") as f:
        data = f.read()

    # IV just sits unauthenticated at the front — nothing ties it to
    # the ciphertext that follows, so an attacker can swap it freely.
    iv = data[:16]
    ciphertext = data[16:]

    cipher = AES.new(KEY, AES.MODE_CBC, iv)  # DANGEROUS: CBC with no MAC
    decrypted_padded = cipher.decrypt(ciphertext)

    try:
        # DANGEROUS: no integrity check happened before this point.
        # Tampered ciphertext just produces garbage plaintext (or a
        # padding exception that gets swallowed below), rather than
        # being rejected outright.
        decrypted = unpad(decrypted_padded, AES.block_size)
    except ValueError:
        # DANGEROUS: silently falls back to the raw (corrupted) bytes
        # instead of refusing to proceed. This also creates a padding
        # oracle: behavior differs subtly for valid vs invalid padding.
        decrypted = decrypted_padded

    with open(output_path, "wb") as f:
        f.write(decrypted)

    # No integrity check ever ran. The caller has no way of knowing
    # whether this file was tampered with — it just silently "worked."


if __name__ == "__main__":
    # Set up a legitimate encrypted file for the demo
    from Crypto.Util.Padding import pad

    plaintext = b"Transfer $100 to account 12345."
    iv = os.urandom(16)
    cipher = AES.new(KEY, AES.MODE_CBC, iv)
    ciphertext = cipher.encrypt(pad(plaintext, AES.block_size))

    with open("payment.enc", "wb") as f:
        f.write(iv + ciphertext)

    insecure_decrypt_file("payment.enc", "payment.dec")
    with open("payment.dec", "rb") as f:
        print("Original decrypt:", f.read())

    # --- ATTACK DEMO: bit-flipping without detection ---
    # CBC mode means flipping a byte in one ciphertext block predictably
    # flips the corresponding byte in the NEXT block's plaintext, with
    # no authentication to catch it.
    with open("payment.enc", "rb") as f:
        tampered = bytearray(f.read())
    tampered[16] ^= 0x01  # flip one bit in the first ciphertext block

    with open("payment_tampered.enc", "wb") as f:
        f.write(tampered)

    insecure_decrypt_file("payment_tampered.enc", "payment_tampered.dec")
    with open("payment_tampered.dec", "rb") as f:
        print("Tampered decrypt (no error raised!):", f.read())