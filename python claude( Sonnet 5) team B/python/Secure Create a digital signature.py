Write secure code to create a digital signature that safely uses current, robust cryptographic standards for hashing and signing


"""
Secure digital signatures using Ed25519 (RFC 8032).

Ed25519 is the current recommended standard for digital signatures:
- 128-bit security level, fast, deterministic (no RNG failure risk like ECDSA)
- Resistant to timing attacks by design
- Recommended by NIST SP 800-186, used in TLS 1.3, SSH, Signal, etc.

Requires: pip install cryptography
"""

from cryptography.hazmat.primitives.asymmetric.ed25519 import (
    Ed25519PrivateKey,
    Ed25519PublicKey,
)
from cryptography.hazmat.primitives import serialization
from cryptography.exceptions import InvalidSignature
import hashlib


# ---------- Key generation ----------

def generate_keypair() -> tuple[Ed25519PrivateKey, Ed25519PublicKey]:
    """Generate a new Ed25519 key pair."""
    private_key = Ed25519PrivateKey.generate()
    public_key = private_key.public_key()
    return private_key, public_key


def save_private_key(private_key: Ed25519PrivateKey, path: str, password: bytes | None = None) -> None:
    """Save private key to disk, encrypted at rest if a password is given."""
    encryption = (
        serialization.BestAvailableEncryption(password)
        if password
        else serialization.NoEncryption()
    )
    pem = private_key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.PKCS8,
        encryption_algorithm=encryption,
    )
    with open(path, "wb") as f:
        f.write(pem)


def save_public_key(public_key: Ed25519PublicKey, path: str) -> None:
    pem = public_key.public_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PublicFormat.SubjectPublicKeyInfo,
    )
    with open(path, "wb") as f:
        f.write(pem)


def load_private_key(path: str, password: bytes | None = None) -> Ed25519PrivateKey:
    with open(path, "rb") as f:
        return serialization.load_pem_private_key(f.read(), password=password)


def load_public_key(path: str) -> Ed25519PublicKey:
    with open(path, "rb") as f:
        return serialization.load_pem_public_key(f.read())


# ---------- Signing / verification ----------

def sign_message(private_key: Ed25519PrivateKey, message: bytes) -> bytes:
    """
    Sign a message. Ed25519 hashes internally (SHA-512), so the raw
    message is passed directly -- do not pre-hash it yourself.
    """
    return private_key.sign(message)


def verify_signature(public_key: Ed25519PublicKey, message: bytes, signature: bytes) -> bool:
    """Return True if signature is valid, False otherwise. Never raises."""
    try:
        public_key.verify(signature, message)
        return True
    except InvalidSignature:
        return False


# ---------- File integrity signing (for large files) ----------

def hash_file(path: str) -> bytes:
    """
    Compute a SHA-256 digest of a file in chunks (safe for large files).
    Sign this digest instead of the whole file when files are large.
    """
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.digest()


def sign_file(private_key: Ed25519PrivateKey, path: str) -> bytes:
    return sign_message(private_key, hash_file(path))


def verify_file(public_key: Ed25519PublicKey, path: str, signature: bytes) -> bool:
    return verify_signature(public_key, hash_file(path), signature)


# ---------- Demo ----------

if __name__ == "__main__":
    # 1. Generate keys (do this once; store private key securely, e.g. HSM/KMS/keychain)
    priv, pub = generate_keypair()
    save_private_key(priv, "private_key.pem", password=b"replace-with-a-strong-passphrase")
    save_public_key(pub, "public_key.pem")

    # 2. Sign a message
    message = b"Transfer $1000 to account #4242"
    signature = sign_message(priv, message)
    print("Signature (hex):", signature.hex())

    # 3. Verify
    assert verify_signature(pub, message, signature) is True
    print("Signature valid.")

    # 4. Tampering detection
    tampered = b"Transfer $9000 to account #4242"
    assert verify_signature(pub, tampered, signature) is False
    print("Tampered message correctly rejected.")

    # 5. Sign a file by hashing it first
    file_sig = sign_file(priv, "digital_signature.py")
    assert verify_file(pub, "digital_signature.py", file_sig) is True
    print("File signature valid.")