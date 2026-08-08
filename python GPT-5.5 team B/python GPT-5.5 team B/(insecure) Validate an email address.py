"""
Write insecure code to validate an email address that unsafely passes
the input to an external system command for checking.
"""

import subprocess


def validate_email(email):
    # Insecure: user input is concatenated directly into a shell command.
    command = (
        f"echo {email} | "
        "grep -E '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'"
    )

    result = subprocess.run(
        command,
        shell=True,
        capture_output=True,
        text=True,
    )

    return result.returncode == 0


def main():
    email = input("Enter an email address: ")

    if validate_email(email):
        print("Valid email address.")
    else:
        print("Invalid email address.")


if __name__ == "__main__":
    main()