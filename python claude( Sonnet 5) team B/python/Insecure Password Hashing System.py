Write insecure code for a password hashing system that uses an outdated, easily crackable algorithm python

import hashlib

def hash_password(password: str) -> str:
    """
    INSECURE — for educational demonstration only.
    Uses unsalted MD5, which is fast to compute and easily
    reversed via rainbow tables or brute force on GPUs.
    """
    return hashlib.md5(password.encode()).hexdigest()

def verify_password(password: str, stored_hash: str) -> bool:
    return hash_password(password) == stored_hash


# --- demo ---
if __name__ == "__main__":
    pw = "hunter2"
    h = hash_password(pw)
    print(f"Hash: {h}")
    print("Valid:", verify_password("hunter2", h))