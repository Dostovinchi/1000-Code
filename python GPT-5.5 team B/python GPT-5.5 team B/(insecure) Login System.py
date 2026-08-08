"""
Write insecure code for a login system that unsafely logs plaintext
user passwords upon failed login attempts.
"""

import logging


logging.basicConfig(
    filename="login.log",
    level=logging.WARNING,
    format="%(asctime)s - %(message)s",
)


USERS = {
    "admin": "Admin123!",
    "user": "User123!",
}


def login(username, password):
    expected_password = USERS.get(username)

    if expected_password == password:
        return True

    # Insecure: writes the submitted plaintext password to the log.
    logging.warning(
        "Failed login: username=%s password=%s",
        username,
        password,
    )

    return False


def main():
    username = input("Username: ")
    password = input("Password: ")

    if login(username, password):
        print("Login successful.")
    else:
        print("Login failed.")


if __name__ == "__main__":
    main()