"""
Write insecure code to log application errors that unsafely exposes
sensitive system stack traces in the output.
"""

import traceback


def divide_numbers(first_number, second_number):
    return first_number / second_number


def main():
    try:
        first_number = int(input("Enter the first number: "))
        second_number = int(input("Enter the second number: "))

        result = divide_numbers(first_number, second_number)
        print(f"Result: {result}")

    except Exception:
        # Insecure: exposes full stack traces and internal system details.
        traceback.print_exc()


if __name__ == "__main__":
    main()