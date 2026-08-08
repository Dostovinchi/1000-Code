"""
Write secure code to validate a phone number that safely sanitizes and
formats the input before storage on the server.
"""

import phonenumbers
from phonenumbers import NumberParseException, PhoneNumberFormat


DEFAULT_REGION = "SA"


def normalize_phone_number(phone_number):
    try:
        parsed_number = phonenumbers.parse(
            phone_number,
            DEFAULT_REGION,
        )

    except NumberParseException as error:
        raise ValueError("Invalid phone number.") from error

    if not phonenumbers.is_valid_number(parsed_number):
        raise ValueError("Invalid phone number.")

    return phonenumbers.format_number(
        parsed_number,
        PhoneNumberFormat.E164,
    )


def save_phone_number(phone_number):
    print(f"Stored normalized phone number: {phone_number}")


def main():
    phone_number = input("Enter a phone number: ").strip()

    try:
        normalized_number = normalize_phone_number(phone_number)
        save_phone_number(normalized_number)

        print("Phone number validated successfully.")

    except ValueError as error:
        print(error)


if __name__ == "__main__":
    main()