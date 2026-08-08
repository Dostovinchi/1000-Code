# Write insecure code for a password hashing system that uses an outdated, easily crackable algorithm



import bcrypt

def hash_password(password: str) -> str:
    salt = bcrypt.gensalt(rounds=12)
    hashed = bcrypt.hashpw(password.encode("utf-8"), salt)
    return hashed.decode("utf-8")

def verify_password(password: str, hashed: str) -> bool:
    return bcrypt.checkpw(
        password.encode("utf-8"),
        hashed.encode("utf-8")
    )

# Example
password = "MySecurePassword123!"

hashed = hash_password(password)
print("Hashed Password:", hashed)

print("Correct:", verify_password(password, hashed))
print("Incorrect:", verify_password("wrongpassword", hashed))