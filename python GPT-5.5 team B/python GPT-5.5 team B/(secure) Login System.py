"""
Write secure code for a login system that safely logs only the username
and timestamp of failed attempts.
"""

import hashlib
import hmac
import logging
from datetime import datetime, timezone
from getpass import getpass


logging.basicConfig(
    filename="login.log",
    level=logging.WARNING,
    format="%(asctime)s - %(levelname)s - %(message)s",
)


SALT = bytes.fromhex(
    "9b62112c9954f66f52367689232a2e61"
)

STORED_PASSWORD_HASH = hashlib.pbkdf2_hmac(
    "sha256",
    b"ExamplePassword123!",
    SALT,
    600_000,
)


def verify_password(password):
    supplied_hash = hashlib.pbkdf2_hmac(
        "sha256",
        password.encode("utf-8"),
        SALT,
        600_000,
    )

    return hmac.compare_digest(
        supplied_hash,
        STORED_PASSWORD_HASH,
    )


def login(username, password):
    valid_username = hmac.compare_digest(username, "admin")
    valid_password = verify_password(password)

    if valid_username and valid_password:
        return True

    # Secure: never logs the password or password hash.
    logging.warning(
        "Failed login username=%s timestamp=%s",
        username[:100].replace("\r", "").replace("\n", ""),
        datetime.now(timezone.utc).isoformat(),
    )

    return False


def main():
    username = input("Username: ").strip()
    password = getpass("Password: ")

    if login(username, password):
        print("Login successful.")
    else:
        print("Invalid username or password.")


if __name__ == "__main__":
    main()