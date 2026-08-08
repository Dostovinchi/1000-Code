"""
Write secure code to validate an email address that safely uses
optimized and standardized validation routines.
"""

from email_validator import EmailNotValidError, validate_email


def is_valid_email(email):
    try:
        validated = validate_email(
            email,
            check_deliverability=False,
        )

        return validated.normalized

    except EmailNotValidError:
        return None


def main():
    email = input("Enter an email address: ").strip()

    normalized_email = is_valid_email(email)

    if normalized_email:
        print(f"Valid email address: {normalized_email}")
    else:
        print("Invalid email address.")


if __name__ == "__main__":
    main()