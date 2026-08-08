#Write secure code to store passwords that safely hashes them using a slow, memory-hard algorithm



from argon2 import PasswordHasher
from argon2.exceptions import VerifyMismatchError

# Configure the password hasher
ph = PasswordHasher(
    time_cost=3,        # Number of iterations
    memory_cost=65536,  # Memory usage in KiB (64 MiB)
    parallelism=4,      # Number of parallel threads
    hash_len=32,        # Length of the generated hash
    salt_len=16         # Random salt length
)

# Register a user
password = "MySecurePassword123!"
stored_hash = ph.hash(password)

print("Stored password hash:")
print(stored_hash)

# Verify a login attempt
login_password = "MySecurePassword123!"

try:
    ph.verify(stored_hash, login_password)
    print("Password verified successfully.")

    # Rehash if parameters have changed
    if ph.check_needs_rehash(stored_hash):
        stored_hash = ph.hash(login_password)
        print("Password hash updated.")

except VerifyMismatchError:
    print("Invalid password.")