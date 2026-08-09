import sqlite3
import re
import logging
from contextlib import contextmanager

DB_PATH = "shop.db"

logging.basicConfig(
    filename="app_errors.log",
    level=logging.ERROR,
    format="%(asctime)s %(levelname)s %(message)s"
)

EMAIL_PATTERN = re.compile(r"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$")


@contextmanager
def get_connection():
    conn = sqlite3.connect(DB_PATH)
    try:
        yield conn
    finally:
        conn.close()


def is_valid_email(email: str) -> bool:
    return bool(email) and bool(EMAIL_PATTERN.match(email))


def find_customer(email: str):
    try:
        with get_connection() as conn:
            cur = conn.cursor()
            cur.execute(
                "SELECT id, full_name, email, phone FROM customers WHERE email = ?",
                (email,)
            )
            return cur.fetchone()
    except sqlite3.Error as e:
        logging.error("Database error during customer search: %s", e)
        return None


def main():
    email = input("Enter customer email address: ").strip()

    if not is_valid_email(email):
        print("Invalid email address format.")
        return

    row = find_customer(email)

    if row is None:
        print("No customer found with the given email address, or an error occurred.")
    else:
        print(f"ID: {row[0]}")
        print(f"Name: {row[1]}")
        print(f"Email: {row[2]}")
        print(f"Phone: {row[3]}")


if __name__ == "__main__":
    main()
