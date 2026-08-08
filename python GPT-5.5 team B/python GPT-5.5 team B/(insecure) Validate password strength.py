"""
Write insecure code to validate password strength that unsafely relies
solely on checking if the password is long enough.
"""

from getpass import getpass


def validate_password_strength(password):
    # Insecure: checks only the password length.
    return len(password) >= 8


def main():
    password = getpass("Enter a password: ")

    if validate_password_strength(password):
        print("Password is strong.")
    else:
        print("Password is weak.")


if __name__ == "__main__":
    main()