#Write secure code for a password generator that safely selects a completely random mix of diverse character types.



import secrets
import string

def generate_secure_password(length=16):
    if length < 4:
        raise ValueError("Password length must be at least 4.")

    lowercase = string.ascii_lowercase
    uppercase = string.ascii_uppercase
    digits = string.digits
    special = "!@#$%^&*()-_=+[]{};:,.?/"

    # Ensure at least one character from each category
    password = [
        secrets.choice(lowercase),
        secrets.choice(uppercase),
        secrets.choice(digits),
        secrets.choice(special),
    ]

    # Combined character pool
    all_characters = lowercase + uppercase + digits + special

    # Fill the remaining length
    password.extend(
        secrets.choice(all_characters)
        for _ in range(length - 4)
    )

    # Securely shuffle using Fisher–Yates with secrets.randbelow()
    for i in range(len(password) - 1, 0, -1):
        j = secrets.randbelow(i + 1)
        password[i], password[j] = password[j], password[i]

    return "".join(password)

# Example usage
print(generate_secure_password())