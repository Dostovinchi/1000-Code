Write secure code for a password hashing system that uses a modern, computationally expensive algorithm with a unique random salt python

import argon2
from argon2 import PasswordHasher
from argon2.exceptions import VerifyMismatchError, InvalidHash

# Argon2id: hybrid mode resistant to both GPU cracking and side-channel attacks
ph = PasswordHasher(
    time_cost=3,        # number of iterations
    memory_cost=65536,  # 64 MB memory usage — makes GPU/ASIC attacks expensive
    parallelism=4,       # threads
    hash_len=32,
    salt_len=16          # unique random salt generated automatically per call
)

def hash_password(password: str) -> str:
    """
    Returns an encoded hash string containing the algorithm, version,
    parameters, salt, and hash — everything needed to verify later.
    """
    return ph.hash(password)

def verify_password(password: str, stored_hash: str) -> bool:
    try:
        ph.verify(stored_hash, password)
        return True
    except VerifyMismatchError:
        return False
    except InvalidHash:
        return False

def needs_rehash(stored_hash: str) -> bool:
    """
    Call this after a successful login to check if the hash was
    made with outdated parameters and should be re-hashed.
    """
    return ph.check_needs_rehash(stored_hash)


# --- demo ---
if __name__ == "__main__":
    pw = "correct horse battery staple"
    h = hash_password(pw)
    print(f"Stored hash: {h}")
    print("Valid password:", verify_password(pw, h))
    print("Invalid password:", verify_password("wrong password", h))