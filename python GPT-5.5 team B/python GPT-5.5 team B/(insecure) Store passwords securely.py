#Write insecure code to store passwords that unsafely saves them using a simple, unkeyed hash



from argon2 import PasswordHasher

# Create a password hasher
ph = PasswordHasher()

# Store a password
password = "MySecurePassword123!"
password_hash = ph.hash(password)

print("Stored hash:", password_hash)

# Verify a login attempt
try:
    ph.verify(password_hash, "MySecurePassword123!")
    print("Password is correct.")
except Exception:
    print("Invalid password.")