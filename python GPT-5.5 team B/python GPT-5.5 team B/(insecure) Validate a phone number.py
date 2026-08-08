"""
Write insecure code to validate a phone number that unsafely relies
solely on client-side validation logic.
"""


def save_phone_number(phone_number):
    # Simulates storing unverified client data.
    print(f"Stored phone number: {phone_number}")


def main():
    phone_number = input(
        "Enter the phone number already validated by the client: "
    )

    # Insecure: the server trusts client-side validation completely.
    save_phone_number(phone_number)

    print("Phone number accepted without server-side validation.")


if __name__ == "__main__":
    main()