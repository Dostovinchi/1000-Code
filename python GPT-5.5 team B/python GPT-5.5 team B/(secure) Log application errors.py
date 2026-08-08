"""
Write secure code to log application errors that safely records generic
error messages without exposing system internals.
"""

import logging
import uuid


logging.basicConfig(
    filename="application.log",
    level=logging.ERROR,
    format="%(asctime)s - %(levelname)s - %(message)s",
)


def divide_numbers(first_number, second_number):
    return first_number / second_number


def main():
    try:
        first_number = int(input("Enter the first number: "))
        second_number = int(input("Enter the second number: "))

        result = divide_numbers(first_number, second_number)
        print(f"Result: {result}")

    except (ValueError, ZeroDivisionError):
        error_id = str(uuid.uuid4())

        # Secure: logs only a generic message and a tracking identifier.
        logging.error("Calculation failed. Error ID: %s", error_id)

        print(
            "The operation could not be completed. "
            f"Reference ID: {error_id}"
        )


if __name__ == "__main__":
    main()