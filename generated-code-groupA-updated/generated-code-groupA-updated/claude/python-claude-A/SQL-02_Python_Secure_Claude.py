import sqlite3
import hashlib
import os
import re
import logging
from contextlib import contextmanager

DB_PATH = "shop.db"

logging.basicConfig(
    filename="app_errors.log",
    level=logging.ERROR,
    format="%(asctime)s %(levelname)s %(message)s"
)

USERNAME_PATTERN = re.compile(r"^[A-Za-z0-9._-]{3,50}$")
EMAIL_PATTERN = re.compile(r"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$")
PHONE_PATTERN = re.compile(r"^[+]?[0-9\s-]{7,20}$")
NAME_PATTERN = re.compile(r"^[A-Za-z\s.'-]{1,100}$")
MAX_PASSWORD_LENGTH = 128
PBKDF2_ITERATIONS = 120_000


@contextmanager
def get_connection():
    conn = sqlite3.connect(DB_PATH)
    try:
        yield conn
    finally:
        conn.close()


def init_db():
    with get_connection() as conn:
        cur = conn.cursor()
        cur.execute("""
            CREATE TABLE IF NOT EXISTS customers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                full_name TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                phone TEXT NOT NULL
            )
        """)
        conn.commit()


def hash_password(password: str, salt: bytes) -> str:
    dk = hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, PBKDF2_ITERATIONS)
    return dk.hex()


def is_valid(value: str, pattern: re.Pattern) -> bool:
    return bool(value) and bool(pattern.match(value))


def is_valid_password(password: str) -> bool:
    return bool(password) and len(password) <= MAX_PASSWORD_LENGTH


def register_customer(username, password, full_name, email, phone) -> bool:
    if not (is_valid(username, USERNAME_PATTERN) and is_valid_password(password)
            and is_valid(full_name, NAME_PATTERN) and is_valid(email, EMAIL_PATTERN)
            and is_valid(phone, PHONE_PATTERN)):
        return False

    salt = os.urandom(16)
    password_hash = hash_password(password, salt)

    try:
        with get_connection() as conn:
            cur = conn.cursor()
            cur.execute(
                "INSERT INTO customers (username, password_hash, salt, full_name, email, phone) "
                "VALUES (?, ?, ?, ?, ?, ?)",
                (username, password_hash, salt.hex(), full_name, email, phone)
            )
            conn.commit()
        return True
    except sqlite3.Error as e:
        logging.error("Database error during registration: %s", e)
        return False


def main():
    try:
        init_db()
    except sqlite3.Error as e:
        logging.error("Database initialization error: %s", e)
        print("A system error occurred. Please try again later.")
        return

    username = input("Enter username: ").strip()
    password = input("Enter password: ")
    full_name = input("Enter full name: ").strip()
    email = input("Enter email address: ").strip()
    phone = input("Enter phone number: ").strip()

    if register_customer(username, password, full_name, email, phone):
        print(f"Registration successful! Welcome, {full_name}.")
    else:
        print("Registration failed. Please check your input and try again.")


if __name__ == "__main__":
    main()
