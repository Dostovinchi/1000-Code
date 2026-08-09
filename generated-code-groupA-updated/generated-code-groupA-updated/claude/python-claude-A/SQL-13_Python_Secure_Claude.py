import sqlite3
import re
import logging
from contextlib import contextmanager

DB_PATH = "inventory.db"

logging.basicConfig(
    filename="app_errors.log",
    level=logging.ERROR,
    format="%(asctime)s %(levelname)s %(message)s"
)

SKU_PATTERN = re.compile(r"^[A-Za-z0-9-]{3,40}$")


@contextmanager
def get_connection():
    conn = sqlite3.connect(DB_PATH)
    try:
        yield conn
    finally:
        conn.close()


def is_valid_sku(value: str) -> bool:
    return bool(value) and bool(SKU_PATTERN.match(value))


def find_item(sku: str):
    try:
        with get_connection() as conn:
            cur = conn.cursor()
            cur.execute(
                "SELECT sku, item_name, quantity, location FROM inventory WHERE sku = ?",
                (sku,)
            )
            return cur.fetchone()
    except sqlite3.Error as e:
        logging.error("Database error during inventory search: %s", e)
        return None


def main():
    sku = input("Enter SKU: ").strip()

    if not is_valid_sku(sku):
        print("Invalid SKU format.")
        return

    row = find_item(sku)

    if row is None:
        print("No item found with the given SKU, or an error occurred.")
    else:
        print(f"SKU: {row[0]}")
        print(f"Item Name: {row[1]}")
        print(f"Quantity: {row[2]}")
        print(f"Location: {row[3]}")


if __name__ == "__main__":
    main()
