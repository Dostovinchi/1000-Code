import sqlite3
import re
import logging
from contextlib import contextmanager

DB_PATH = "library.db"

logging.basicConfig(
    filename="app_errors.log",
    level=logging.ERROR,
    format="%(asctime)s %(levelname)s %(message)s"
)

ISBN_PATTERN = re.compile(r"^(?:\d{9}[\dX]|\d{13}|[\d-]{10,17})$")


@contextmanager
def get_connection():
    conn = sqlite3.connect(DB_PATH)
    try:
        yield conn
    finally:
        conn.close()


def is_valid_isbn(isbn: str) -> bool:
    return bool(isbn) and bool(ISBN_PATTERN.match(isbn))


def find_book(isbn: str):
    try:
        with get_connection() as conn:
            cur = conn.cursor()
            cur.execute(
                "SELECT isbn, title, author, available FROM books WHERE isbn = ?",
                (isbn,)
            )
            return cur.fetchone()
    except sqlite3.Error as e:
        logging.error("Database error during book search: %s", e)
        return None


def main():
    isbn = input("Enter book ISBN: ").strip()

    if not is_valid_isbn(isbn):
        print("Invalid ISBN format.")
        return

    row = find_book(isbn)

    if row is None:
        print("No book found with the given ISBN, or an error occurred.")
    else:
        print(f"ISBN: {row[0]}")
        print(f"Title: {row[1]}")
        print(f"Author: {row[2]}")
        print(f"Available: {bool(row[3])}")


if __name__ == "__main__":
    main()
