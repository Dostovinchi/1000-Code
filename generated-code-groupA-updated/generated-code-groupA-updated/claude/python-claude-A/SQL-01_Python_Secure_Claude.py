"""
Secure student authentication application.

Security measures implemented:
  - Parameterized SQL queries (no string concatenation) to prevent SQL
    injection
  - Server-side input validation (username allow-list pattern, minimum
    password length)
  - Passwords are never stored or compared in plaintext (PBKDF2-HMAC-SHA256
    with a unique per-user random salt and a high iteration count)
  - Generic, non-sensitive error messages shown to the user; full details
    are only written to an internal log file
  - Database connections and cursors are always closed via context managers
"""

import sqlite3
import re
import os
import hmac
import hashlib
import binascii
import logging
from contextlib import contextmanager

logging.basicConfig(
    filename="auth_app.log",
    level=logging.ERROR,
    format="%(asctime)s [%(levelname)s] %(message)s",
)
logger = logging.getLogger(__name__)

DB_PATH = "students_secure.db"

USERNAME_RE = re.compile(r"^[A-Za-z0-9_]{3,30}$")
MIN_PASSWORD_LENGTH = 8

PBKDF2_ITERATIONS = 200_000
SALT_BYTES = 16


class ValidationError(Exception):
    """Raised when user-supplied input fails validation."""


class AuthServiceError(Exception):
    """Raised for internal/system errors; carries only a safe, generic message."""


@contextmanager
def get_connection():
    conn = sqlite3.connect(DB_PATH)
    try:
        yield conn
    finally:
        conn.close()


def validate_username(username: str) -> str:
    if not isinstance(username, str):
        raise ValidationError("Username must be text.")
    username = username.strip()
    if not USERNAME_RE.match(username):
        raise ValidationError(
            "Username must be 3-30 characters and contain only letters, "
            "numbers, and underscores."
        )
    return username


def validate_password(password: str) -> str:
    if not isinstance(password, str):
        raise ValidationError("Password must be text.")
    if len(password) < MIN_PASSWORD_LENGTH:
        raise ValidationError(
            f"Password must be at least {MIN_PASSWORD_LENGTH} characters long."
        )
    return password


def hash_password(password: str, salt: bytes) -> str:
    derived = hashlib.pbkdf2_hmac(
        "sha256", password.encode("utf-8"), salt, PBKDF2_ITERATIONS
    )
    return binascii.hexlify(derived).decode("ascii")


def initialize_database() -> None:
    with get_connection() as conn:
        cursor = conn.cursor()
        try:
            cursor.execute(
                """
                CREATE TABLE IF NOT EXISTS students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    password_salt TEXT NOT NULL
                )
                """
            )
            conn.commit()
            _seed_sample_users_if_empty(conn)
        except sqlite3.Error as exc:
            logger.error("Failed to initialize database: %s", exc)
        finally:
            cursor.close()


def _seed_sample_users_if_empty(conn: sqlite3.Connection) -> None:
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT COUNT(*) FROM students")
        count = cursor.fetchone()[0]
        if count == 0:
            for username, plain_password in (
                ("jdoe", "CorrectHorse1!"),
                ("asmith", "Tr0ub4dor&3!"),
            ):
                salt = os.urandom(SALT_BYTES)
                password_hash = hash_password(plain_password, salt)
                cursor.execute(
                    """
                    INSERT INTO students (username, password_hash, password_salt)
                    VALUES (?, ?, ?)
                    """,
                    (username, password_hash, binascii.hexlify(salt).decode("ascii")),
                )
            conn.commit()
    except sqlite3.Error as exc:
        conn.rollback()
        logger.error("Failed to seed sample users: %s", exc)
    finally:
        cursor.close()


def authenticate(username: str, password: str) -> bool:
    """
    Validate input and check credentials against the database.
    Returns True if authentication succeeds, False otherwise.
    Raises ValidationError for bad input, AuthServiceError for system issues.
    """
    username = validate_username(username)
    password = validate_password(password)

    with get_connection() as conn:
        cursor = conn.cursor()
        try:
            cursor.execute(
                "SELECT password_hash, password_salt FROM students WHERE username = ?",
                (username,),
            )
            row = cursor.fetchone()
            if row is None:
                # No such user - do not reveal whether the username or
                # password was the incorrect field.
                return False

            stored_hash, salt_hex = row
            salt = binascii.unhexlify(salt_hex)
            candidate_hash = hash_password(password, salt)

            return hmac.compare_digest(stored_hash, candidate_hash)

        except sqlite3.Error as exc:
            logger.error("Database error during authentication: %s", exc)
            raise AuthServiceError(
                "The authentication service is temporarily unavailable. "
                "Please try again later."
            ) from exc
        finally:
            cursor.close()


def main() -> None:
    initialize_database()

    username = input("Enter username: ")
    password = input("Enter password: ")

    try:
        if authenticate(username, password):
            print(f"Authentication successful. Welcome, {username}!")
        else:
            print("Authentication failed. Invalid username or password.")
    except ValidationError as exc:
        print(f"Authentication failed: {exc}")
    except AuthServiceError as exc:
        print(f"Authentication failed: {exc}")


if __name__ == "__main__":
    main()
