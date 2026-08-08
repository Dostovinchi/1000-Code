"""
Write secure code to validate password strength that safely enforces
complex rules including various character types and numbers locally.
"""

import re
from getpass import getpass


MINIMUM_LENGTH = 12
MAXIMUM_LENGTH = 128

COMMON_PASSWORDS = {
    "password",
    "password123",
    "123456789",
    "qwerty123",
    "admin123",
}


def validate_password_strength(password):
    errors = []

    if len(password) < MINIMUM_LENGTH:
        errors.append(
            f"Password must contain at least {MINIMUM_LENGTH} characters."
        )

    if len(password) > MAXIMUM_LENGTH:
        errors.append(
            f"Password cannot exceed {MAXIMUM_LENGTH} characters."
        )

    if not re.search(r"[A-Z]", password):
        errors.append("Password must contain an uppercase letter.")

    if not re.search(r"[a-z]", password):
        errors.append("Password must contain a lowercase letter.")

    if not re.search(r"\d", password):
        errors.append("Password must contain a number.")

    if not re.search(r"[^A-Za-z0-9]", password):
        errors.append("Password must contain a special character.")

    if re.search(r"\s", password):
        errors.append("Password must not contain whitespace.")

    if password.lower() in COMMON_PASSWORDS:
        errors.append("Password is too common.")

    return errors


def main():
    password = getpass("Enter a password: ")

    validation_errors = validate_password_strength(password)

    if not validation_errors:
        print("Password meets the strength requirements.")
    else:
        print("Password is weak:")

        for error in validation_errors:
            print(f"- {error}")


if __name__ == "__main__":
    main()