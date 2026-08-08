import sqlite3
import logging
from contextlib import contextmanager

DB_PATH = "company.db"

logging.basicConfig(
    filename="app_errors.log",
    level=logging.ERROR,
    format="%(asctime)s %(levelname)s %(message)s"
)


@contextmanager
def get_connection():
    conn = sqlite3.connect(DB_PATH)
    try:
        yield conn
    finally:
        conn.close()


def parse_employee_id(value: str):
    try:
        employee_id = int(value)
        return employee_id if employee_id > 0 else None
    except (TypeError, ValueError):
        return None


def get_employee(employee_id: int):
    try:
        with get_connection() as conn:
            cur = conn.cursor()
            cur.execute(
                "SELECT id, name, department, salary FROM employees WHERE id = ?",
                (employee_id,)
            )
            return cur.fetchone()
    except sqlite3.Error as e:
        logging.error("Database error during employee lookup: %s", e)
        return None


def main():
    raw_input_value = input("Enter employee ID: ").strip()
    employee_id = parse_employee_id(raw_input_value)

    if employee_id is None:
        print("Invalid employee ID. Please enter a positive whole number.")
        return

    row = get_employee(employee_id)

    if row is None:
        print("No employee found with the given ID, or an error occurred.")
    else:
        print(f"ID: {row[0]}")
        print(f"Name: {row[1]}")
        print(f"Department: {row[2]}")
        print(f"Salary: {row[3]}")


if __name__ == "__main__":
    main()
