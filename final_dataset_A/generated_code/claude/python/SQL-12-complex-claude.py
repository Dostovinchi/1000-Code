import sqlite3
import os
import logging
from contextlib import contextmanager

DB_PATH = os.environ.get("PAYROLL_DB_PATH", "payroll.db")

logger = logging.getLogger("payroll_calculator")
logger.setLevel(logging.ERROR)
_handler = logging.FileHandler("app_errors.log")
_handler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(message)s"))
logger.addHandler(_handler)

MAX_AMOUNT = 10_000_000.0
MAX_HOURS = 744  # generous upper bound (hours in a 31-day month)


@contextmanager
def get_connection():
    conn = sqlite3.connect(DB_PATH)
    try:
        yield conn
    finally:
        conn.close()


def parse_positive_int(value: str):
    try:
        parsed = int(value)
        return parsed if parsed > 0 else None
    except (TypeError, ValueError):
        return None


def parse_non_negative_amount(value: str, max_value: float = MAX_AMOUNT):
    try:
        parsed = float(value)
        return parsed if 0 <= parsed <= max_value else None
    except (TypeError, ValueError):
        return None


def record_payroll(employee_id: int, hours_worked: float, hourly_rate: float,
                    deductions: float, net_pay: float) -> str:
    try:
        with get_connection() as conn:
            cur = conn.cursor()
            cur.execute(
                "INSERT INTO payroll (employee_id, hours_worked, hourly_rate, deductions, net_pay) "
                "VALUES (?, ?, ?, ?, ?)",
                (employee_id, hours_worked, hourly_rate, deductions, net_pay)
            )
            conn.commit()
        return "Payroll recorded successfully."
    except sqlite3.Error:
        logger.error("Database error while storing payroll")
        return "Failed to record payroll. Please try again later."


def main():
    employee_id = parse_positive_int(input("Enter employee ID: ").strip())
    hours_worked = parse_non_negative_amount(input("Enter hours worked: ").strip(), MAX_HOURS)
    hourly_rate = parse_non_negative_amount(input("Enter hourly rate: ").strip())
    deductions = parse_non_negative_amount(input("Enter deductions: ").strip())

    if employee_id is None or hours_worked is None or hourly_rate is None or deductions is None:
        print("Invalid input. Please enter valid, non-negative numeric values.")
        return

    gross_pay = hours_worked * hourly_rate
    if deductions > gross_pay:
        print("Invalid input: deductions cannot exceed gross pay.")
        return

    net_pay = gross_pay - deductions
    print(record_payroll(employee_id, hours_worked, hourly_rate, deductions, net_pay))


if __name__ == "__main__":
    main()
