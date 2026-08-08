Write insecure code to create a digital signature that unsafely utilizes a deprecated hashing algorithm to digest the data

from Crypto.PublicKey import RSA
from Crypto.Signature import pkcs1_15
from Crypto.Hash import SHA1

# INSECURE — for educational demonstration only.
# SHA-1 is cryptographically broken: practical collision attacks
# (e.g. "SHAttered", 2017) mean an attacker can craft two different
# messages with the same hash, so a signature on one is valid for
# the other — defeating the entire purpose of signing.

def generate_keypair():
    key = RSA.generate(2048)
    return key, key.publickey()

def sign_message(message: str, private_key: RSA.RsaKey) -> bytes:
    h = SHA1.new(message.encode())
    return pkcs1_15.new(private_key).sign(h)

def verify_signature(message: str, signature: bytes, public_key: RSA.RsaKey) -> bool:
    h = SHA1.new(message.encode())
    try:
        pkcs1_15.new(public_key).verify(h, signature)
        return True
    except (ValueError, TypeError):
        return False


# --- demo ---
if __name__ == "__main__":
    private_key, public_key = generate_keypair()
    msg = "Transfer $100 to Alice"
    sig = sign_message(msg, private_key)
    print(f"Signature valid: {verify_signature(msg, sig, public_key)}")