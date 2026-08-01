import sqlite3
import os
import re
import logging
from contextlib import contextmanager

DB_PATH = os.environ.get("BANK_DB_PATH", "bank.db")

logger = logging.getLogger("funds_transfer")
logger.setLevel(logging.ERROR)
_handler = logging.FileHandler("app_errors.log")
_handler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(message)s"))
logger.addHandler(_handler)

ACCOUNT_PATTERN = re.compile(r"^[A-Za-z0-9-]{6,30}$")


@contextmanager
def get_connection():
    conn = sqlite3.connect(DB_PATH)
    try:
        yield conn
    finally:
        conn.close()


def is_valid_account(value: str) -> bool:
    return bool(value) and bool(ACCOUNT_PATTERN.match(value))


def parse_amount(value: str):
    try:
        amount = float(value)
        return amount if amount > 0 else None
    except (TypeError, ValueError):
        return None


def transfer_funds(source_account: str, target_account: str, amount: float) -> str:
    try:
        with get_connection() as conn:
            cur = conn.cursor()
            try:
                cur.execute("BEGIN")

                cur.execute(
                    "SELECT balance FROM accounts WHERE account_number = ?",
                    (source_account,)
                )
                source_row = cur.fetchone()
                if source_row is None:
                    conn.rollback()
                    return "Transfer failed: source account does not exist."

                if source_row[0] < amount:
                    conn.rollback()
                    return "Transfer failed: insufficient funds."

                cur.execute(
                    "SELECT 1 FROM accounts WHERE account_number = ?",
                    (target_account,)
                )
                if cur.fetchone() is None:
                    conn.rollback()
                    return "Transfer failed: target account does not exist."

                cur.execute(
                    "UPDATE accounts SET balance = balance - ? WHERE account_number = ?",
                    (amount, source_account)
                )
                cur.execute(
                    "UPDATE accounts SET balance = balance + ? WHERE account_number = ?",
                    (amount, target_account)
                )

                conn.commit()
                logger.info("Funds transfer completed successfully")
                return "Transfer successful."

            except sqlite3.Error:
                conn.rollback()
                raise

    except sqlite3.Error:
        logger.error("Database error during funds transfer")
        return "Transfer failed due to a system error. Please try again later."


def main():
    source_account = input("Enter source account number: ").strip()
    target_account = input("Enter target account number: ").strip()
    amount_input = input("Enter transfer amount: ").strip()

    if not is_valid_account(source_account) or not is_valid_account(target_account):
        print("Invalid account number format.")
        return

    if source_account == target_account:
        print("Source and target accounts must be different.")
        return

    amount = parse_amount(amount_input)
    if amount is None:
        print("Invalid transfer amount. Enter a positive number.")
        return

    print(transfer_funds(source_account, target_account, amount))


if __name__ == "__main__":
    main()
