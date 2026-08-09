import sqlite3
import os
import re
import logging
from contextlib import contextmanager

DB_PATH = os.environ.get("FINANCE_DB_PATH", "finance.db")

logger = logging.getLogger("expense_recorder")
logger.setLevel(logging.ERROR)
_handler = logging.FileHandler("app_errors.log")
_handler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(message)s"))
logger.addHandler(_handler)

DESCRIPTION_PATTERN = re.compile(r"^[\w\s.,'&()-]{1,255}$", re.UNICODE)
CATEGORY_PATTERN = re.compile(r"^[A-Za-z\s&-]{1,50}$")
MAX_AMOUNT = 1_000_000.0


@contextmanager
def get_connection():
    conn = sqlite3.connect(DB_PATH)
    try:
        yield conn
    finally:
        conn.close()


def is_valid_description(value: str) -> bool:
    return bool(value) and bool(DESCRIPTION_PATTERN.match(value))


def is_valid_category(value: str) -> bool:
    return bool(value) and bool(CATEGORY_PATTERN.match(value))


def parse_amount(value: str):
    try:
        amount = float(value)
        return amount if 0 <= amount <= MAX_AMOUNT else None
    except (TypeError, ValueError):
        return None


def record_expense(description: str, category: str, amount: float) -> str:
    try:
        with get_connection() as conn:
            cur = conn.cursor()
            cur.execute(
                "INSERT INTO expenses (description, category, amount) VALUES (?, ?, ?)",
                (description, category, amount)
            )
            conn.commit()
        return "Expense transaction saved successfully."
    except sqlite3.Error:
        logger.error("Database error while recording expense")
        return "Failed to save the expense transaction. Please try again later."


def main():
    description = input("Enter expense description: ").strip()
    category = input("Enter category: ").strip()
    amount_input = input("Enter amount: ").strip()

    if not is_valid_description(description):
        print("Invalid description.")
        return

    if not is_valid_category(category):
        print("Invalid category.")
        return

    amount = parse_amount(amount_input)
    if amount is None:
        print("Invalid amount. Enter a non-negative number.")
        return

    print(record_expense(description, category, amount))


if __name__ == "__main__":
    main()
