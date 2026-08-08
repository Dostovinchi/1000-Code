Write secure code to encrypt a file using RSA that safely utilizes optimal, randomized asymmetric encryption padding 

"""
Secure file encryption using hybrid RSA-OAEP + AES-256-GCM.

Why hybrid encryption?
RSA-OAEP can only encrypt data smaller than the key size (e.g. a 4096-bit
key can encrypt at most ~446 bytes). Files are almost always larger than
that, so we:

  1. Generate a random 256-bit AES key + 96-bit nonce.
  2. Encrypt the file with AES-256-GCM (fast, authenticated encryption).
  3. Encrypt the AES key with RSA-OAEP (MGF1/SHA-256) -- this is the
     "optimal, randomized asymmetric encryption padding" you asked for.
  4. Package everything into one output file.

RSA-OAEP is randomized (a fresh random seed is used internally on every
call) and provides IND-CCA2 security, unlike deterministic PKCS#1 v1.5
padding, which is vulnerable to padding-oracle attacks (e.g. Bleichenbacher).

Usage:
    python rsa_file_crypto.py genkey --out mykey
    python rsa_file_crypto.py encrypt --pubkey mykey_public.pem --in secret.pdf --out secret.pdf.enc
    python rsa_file_crypto.py decrypt --privkey mykey_private.pem --in secret.pdf.enc --out secret.pdf
"""

import argparse
import os
import struct
import sys

from cryptography.hazmat.primitives.asymmetric import rsa, padding as asym_padding
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.ciphers.aead import AESGCM

MAGIC = b"RSAF"          # simple format tag
VERSION = 1
AES_KEY_SIZE = 32        # 256-bit AES key
NONCE_SIZE = 12          # 96-bit GCM nonce (standard, do not change)
RSA_KEY_SIZE_BITS = 4096 # strong default; 3072 is also acceptable


def oaep_padding() -> asym_padding.OAEP:
    """
    OAEP padding using MGF1(SHA-256). This is the modern, recommended
    configuration -- randomized, IND-CCA2 secure when implemented correctly
    (as this library does), and resistant to chosen-ciphertext attacks that
    break naive textbook RSA or PKCS#1 v1.5.
    """
    return asym_padding.OAEP(
        mgf=asym_padding.MGF1(algorithm=hashes.SHA256()),
        algorithm=hashes.SHA256(),
        label=None,
    )


# ----------------------------------------------------------------------
# Key generation
# ----------------------------------------------------------------------

def generate_keypair(out_prefix: str, key_size: int = RSA_KEY_SIZE_BITS,
                      password: bytes | None = None) -> None:
    private_key = rsa.generate_private_key(
        public_exponent=65537,
        key_size=key_size,
    )
    public_key = private_key.public_key()

    encryption = (
        serialization.BestAvailableEncryption(password)
        if password
        else serialization.NoEncryption()
    )

    priv_bytes = private_key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.PKCS8,
        encryption_algorithm=encryption,
    )
    pub_bytes = public_key.public_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PublicFormat.SubjectPublicKeyInfo,
    )

    priv_path = f"{out_prefix}_private.pem"
    pub_path = f"{out_prefix}_public.pem"

    # Private key should never be world-readable.
    fd = os.open(priv_path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
    with os.fdopen(fd, "wb") as f:
        f.write(priv_bytes)

    with open(pub_path, "wb") as f:
        f.write(pub_bytes)

    print(f"Private key written to {priv_path} (mode 600)")
    print(f"Public key written to {pub_path}")


# ----------------------------------------------------------------------
# Encryption
# ----------------------------------------------------------------------

def load_public_key(path: str):
    with open(path, "rb") as f:
        return serialization.load_pem_public_key(f.read())


def load_private_key(path: str, password: bytes | None = None):
    with open(path, "rb") as f:
        return serialization.load_pem_private_key(f.read(), password=password)


def encrypt_file(pubkey_path: str, in_path: str, out_path: str) -> None:
    public_key = load_public_key(pubkey_path)

    if public_key.key_size < 2048:
        raise ValueError("Refusing to use an RSA key smaller than 2048 bits.")

    # Fresh random AES key + nonce per encryption. Never reuse a
    # (key, nonce) pair with AES-GCM.
    aes_key = os.urandom(AES_KEY_SIZE)
    nonce = os.urandom(NONCE_SIZE)

    with open(in_path, "rb") as f:
        plaintext = f.read()

    aesgcm = AESGCM(aes_key)
    # No associated data needed here; add some (e.g. filename) if you want
    # to bind ciphertext to context.
    ciphertext = aesgcm.encrypt(nonce, plaintext, associated_data=None)

    # RSA-OAEP encryption of the AES key -- randomized under the hood.
    encrypted_key = public_key.encrypt(aes_key, oaep_padding())

    with open(out_path, "wb") as f:
        f.write(MAGIC)
        f.write(struct.pack(">B", VERSION))
        f.write(struct.pack(">H", len(encrypted_key)))
        f.write(encrypted_key)
        f.write(struct.pack(">B", len(nonce)))
        f.write(nonce)
        f.write(ciphertext)  # AESGCM.encrypt() appends the 16-byte auth tag

    print(f"Encrypted '{in_path}' -> '{out_path}'")


# ----------------------------------------------------------------------
# Decryption
# ----------------------------------------------------------------------

def decrypt_file(privkey_path: str, in_path: str, out_path: str,
                  password: bytes | None = None) -> None:
    private_key = load_private_key(privkey_path, password=password)

    with open(in_path, "rb") as f:
        data = f.read()

    offset = 0
    magic = data[offset:offset + 4]; offset += 4
    if magic != MAGIC:
        raise ValueError("Not a recognized encrypted file (bad magic).")

    version = data[offset]; offset += 1
    if version != VERSION:
        raise ValueError(f"Unsupported format version: {version}")

    key_len = struct.unpack(">H", data[offset:offset + 2])[0]; offset += 2
    encrypted_key = data[offset:offset + key_len]; offset += key_len

    nonce_len = data[offset]; offset += 1
    nonce = data[offset:offset + nonce_len]; offset += nonce_len

    ciphertext = data[offset:]

    try:
        aes_key = private_key.decrypt(encrypted_key, oaep_padding())
    except ValueError:
        # Deliberately generic: don't leak *why* RSA decryption/padding
        # failed, to avoid oracle-style attacks.
        raise ValueError("Decryption failed: invalid key or corrupted file.")

    aesgcm = AESGCM(aes_key)
    try:
        plaintext = aesgcm.decrypt(nonce, ciphertext, associated_data=None)
    except Exception:
        # AES-GCM authentication failure -- file was tampered with or
        # wrong key.
        raise ValueError("Decryption failed: authentication check failed "
                          "(file may be corrupted or tampered with).")

    with open(out_path, "wb") as f:
        f.write(plaintext)

    print(f"Decrypted '{in_path}' -> '{out_path}'")


# ----------------------------------------------------------------------
# CLI
# ----------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                      formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="command", required=True)

    p_gen = sub.add_parser("genkey", help="Generate an RSA keypair")
    p_gen.add_argument("--out", required=True, help="Output filename prefix")
    p_gen.add_argument("--bits", type=int, default=RSA_KEY_SIZE_BITS)

    p_enc = sub.add_parser("encrypt", help="Encrypt a file")
    p_enc.add_argument("--pubkey", required=True)
    p_enc.add_argument("--in", dest="in_path", required=True)
    p_enc.add_argument("--out", required=True)

    p_dec = sub.add_parser("decrypt", help="Decrypt a file")
    p_dec.add_argument("--privkey", required=True)
    p_dec.add_argument("--in", dest="in_path", required=True)
    p_dec.add_argument("--out", required=True)

    args = parser.parse_args()

    if args.command == "genkey":
        generate_keypair(args.out, key_size=args.bits)
    elif args.command == "encrypt":
        encrypt_file(args.pubkey, args.in_path, args.out)
    elif args.command == "decrypt":
        decrypt_file(args.privkey, args.in_path, args.out)


if __name__ == "__main__":
    main()