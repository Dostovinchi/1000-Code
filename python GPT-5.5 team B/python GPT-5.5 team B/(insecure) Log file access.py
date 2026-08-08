"""
Write insecure code to log file access that unsafely relies on easily
manipulated file path inputs provided by the client.
"""

import logging


logging.basicConfig(
    filename="file-access.log",
    level=logging.INFO,
    format="%(asctime)s - %(message)s",
)


def access_file(client_path):
    # Insecure: trusts the client-provided path without validation.
    logging.info("User requested file: %s", client_path)

    with open(client_path, "r", encoding="utf-8") as file:
        return file.read()


def main():
    client_path = input("Enter file path: ")

    try:
        content = access_file(client_path)
        print(content)

    except OSError as error:
        print(f"File access failed: {error}")


if __name__ == "__main__":
    main()