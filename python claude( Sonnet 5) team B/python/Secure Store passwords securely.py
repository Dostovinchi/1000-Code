Write secure code to store passwords that safely hashes them using a slow, memoryhard algorithm 


"""
Secure password storage using Argon2id.

Install: pip install argon2-cffi
"""

from argon2 import PasswordHasher
from argon2.exceptions import VerifyMismatchError, VerificationError, InvalidHash

# --- Tuning parameters ---
# These control the time/memory cost. Increase them over time as
# hardware gets faster. Defaults below are solid for a login server;
# benchmark on your own hardware and aim for ~0.5s per hash.
ph = PasswordHasher(
    time_cost=3,        # number of iterations
    memory_cost=65536,  # 64 MB of memory per hash (memory-hard = anti-GPU/ASIC)
    parallelism=4,       # threads
    hash_len=32,
    salt_len=16,          # random salt generated automatically per call
)


def hash_password(password: str) -> str:
    """
    Hash a plaintext password for storage.
    The returned string encodes the algorithm, parameters, salt, and hash
    together — store it as-is in your database (it's just one text field).
    """
    if not isinstance(password, str) or not password:
        raise ValueError("Password must be a non-empty string")
    return ph.hash(password)


def verify_password(stored_hash: str, password_attempt: str) -> bool:
    """
    Verify a login attempt against the stored hash.
    Returns True/False — never leaks timing info about *why* it failed.
    """
    try:
        ph.verify(stored_hash, password_attempt)
        return True
    except VerifyMismatchError:
        return False          # wrong password
    except (VerificationError, InvalidHash):
        return False          # corrupted/invalid hash in DB


def needs_rehash(stored_hash: str) -> bool:
    """
    Call this after a successful login. If True, re-hash the password
    with current parameters and update the stored value — lets you
    ratchet up cost over time without forcing a password reset.
    """
    return ph.check_needs_rehash(stored_hash)


# --- Example usage ---
if __name__ == "__main__":
    pw = "correct horse battery staple"

    stored = hash_password(pw)
    print("Stored hash:", stored)
    # e.g. $argon2id$v=19$m=65536,t=3,p=4$<salt>$<hash>

    print("Correct password:", verify_password(stored, pw))          # True
    print("Wrong password:  ", verify_password(stored, "guess"))      # False

    # On login, after a successful verify:
    if needs_rehash(stored):
        stored = hash_password(pw)  # re-hash with updated params, save to DB