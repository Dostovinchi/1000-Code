import sqlite3
import re
import logging
from contextlib import contextmanager

DB_PATH = "store.db"

logging.basicConfig(
    filename="app_errors.log",
    level=logging.ERROR,
    format="%(asctime)s %(levelname)s %(message)s"
)

NAME_PATTERN = re.compile(r"^[A-Za-z0-9\s.,'&-]{1,100}$")


@contextmanager
def get_connection():
    conn = sqlite3.connect(DB_PATH)
    try:
        yield conn
    finally:
        conn.close()


def is_valid_search_term(term: str) -> bool:
    return bool(term) and bool(NAME_PATTERN.match(term))


def escape_like(term: str) -> str:
    return term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")


def search_products(product_name: str):
    if not is_valid_search_term(product_name):
        return None

    try:
        with get_connection() as conn:
            cur = conn.cursor()
            cur.execute(
                "SELECT id, name, price, quantity FROM products WHERE name LIKE ? ESCAPE '\\' LIMIT 100",
                (f"%{escape_like(product_name)}%",)
            )
            return cur.fetchall()
    except sqlite3.Error as e:
        logging.error("Database error during product search: %s", e)
        return None


def main():
    product_name = input("Enter product name to search: ").strip()
    results = search_products(product_name)

    if results is None:
        print("Search failed. Please check your input and try again.")
    elif results:
        for row in results:
            print(f"ID: {row[0]}, Name: {row[1]}, Price: {row[2]}, Quantity: {row[3]}")
    else:
        print("No products found matching your search.")


if __name__ == "__main__":
    main()
