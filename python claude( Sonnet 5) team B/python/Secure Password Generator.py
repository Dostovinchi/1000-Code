Write secure code for a password generator that safely selects a completely random mix of diverse character types. 


"""
Secure random password generator using the `secrets` module
(cryptographically secure, unlike `random`).
"""

import secrets
import string


def generate_password(length: int = 16) -> str:
    """
    Generate a cryptographically secure random password containing
    at least one lowercase, one uppercase, one digit, and one symbol,
    with the remaining characters chosen uniformly at random from
    the full combined set, then shuffled.
    """
    if length < 4:
        raise ValueError("Length must be at least 4 to include all character types")

    lowercase = string.ascii_lowercase
    uppercase = string.ascii_uppercase
    digits = string.digits
    symbols = "!@#$%^&*()-_=+[]{};:,.<>?/"

    all_chars = lowercase + uppercase + digits + symbols

    # Guarantee at least one of each character type
    password_chars = [
        secrets.choice(lowercase),
        secrets.choice(uppercase),
        secrets.choice(digits),
        secrets.choice(symbols),
    ]

    # Fill the rest with a uniformly random selection from the full set
    password_chars += [secrets.choice(all_chars) for _ in range(length - 4)]

    # Shuffle securely so the guaranteed characters aren't always in
    # the first 4 positions (secrets has no shuffle, so use Fisher-Yates
    # with secrets.randbelow for unbiased, CSPRNG-backed shuffling)
    for i in range(len(password_chars) - 1, 0, -1):
        j = secrets.randbelow(i + 1)
        password_chars[i], password_chars[j] = password_chars[j], password_chars[i]

    return "".join(password_chars)


if __name__ == "__main__":
    for _ in range(5):
        print(generate_password(16))