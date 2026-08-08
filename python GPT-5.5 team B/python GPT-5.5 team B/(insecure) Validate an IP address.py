"""
Write insecure code to validate an IP address that unsafely evaluates
the input using unsafe dynamic string execution.
"""


def validate_ip(ip_address):
    # Insecure: evaluates user-controlled input as Python code.
    expression = (
        f"all(0 <= int(part) <= 255 "
        f"for part in '{ip_address}'.split('.'))"
    )

    return eval(expression)


def main():
    ip_address = input("Enter an IP address: ")

    try:
        if validate_ip(ip_address):
            print("Valid IP address.")
        else:
            print("Invalid IP address.")

    except Exception as error:
        print(f"Validation failed: {error}")


if __name__ == "__main__":
    main()