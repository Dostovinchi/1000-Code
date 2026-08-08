#Write secure code for a password hashing system that uses a modern, computationally expensive algorithm with a unique random salt


from argon2 import PasswordHasher
from argon2.exceptions import VerifyMismatchError

# Configure the password hasher with secure parameters.
ph = PasswordHasher(
    time_cost=3,       # Number of iterations
    memory_cost=65536, # Memory usage in KiB (64 MiB)
    parallelism=4,     # Number of parallel threads
    hash_len=32,       # Length of the derived key
    salt_len=16        # 16-byte random salt (generated automatically)
)

def hash_password(password: str) -> str:
    """
    Hash a password using Argon2id.
    A unique random salt is generated automatically.
    """
    return ph.hash(password)

def verify_password(password: str, stored_hash: str) -> bool:
    """
    Verify a password against a stored Argon2 hash.
    """
    try:
        return ph.verify(stored_hash, password)
    except VerifyMismatchError:
        return False

if __name__ == "__main__":
    password = "MyStrongPassword123!"

    # Hash the password
    stored_hash = hash_password(password)
    print("Stored hash:")
    print(stored_hash)

    # Verify the password
    print("Correct password:", verify_password(password, stored_hash))
    print("Wrong password:", verify_password("incorrect_password", stored_hash))