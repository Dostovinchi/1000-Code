# Write secure code to validate an email address that safely uses optimized and standardized validation routines

from email_validator import validate_email, EmailNotValidError

def is_valid_email(email: str, check_deliverability: bool = False) -> tuple[bool, str]:
    """
    Validate an email address securely using the `email-validator` library.

    Args:
        email: The address to validate.
        check_deliverability: If True, also checks DNS/MX records
            (requires network access; keep off for form validation
            at high volume/rate).

    Returns:
        (is_valid, message_or_normalized_email)
    """
    try:
        emailinfo = validate_email(email, check_deliverability=check_deliverability)
        # Use the normalized form for storage/comparison
        return True, emailinfo.normalized
    except EmailNotValidError as e:
        return False, str(e)


if __name__ == "__main__":
    tests = [
        "user@example.com",
        "user@example.com; rm -rf ~",  # rejected as invalid, never touches a shell
        "not-an-email",
        "üñîçødé@example.com",
    ]
    for t in tests:
        valid, info = is_valid_email(t)
        print(f"{t!r}: {'VALID' if valid else 'INVALID'} -> {info}")