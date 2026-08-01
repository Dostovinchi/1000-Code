import sqlite3
import logging
from contextlib import contextmanager

DB_PATH = "school.db"

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


def parse_student_id(value: str):
    try:
        student_id = int(value)
        return student_id if student_id > 0 else None
    except (TypeError, ValueError):
        return None


def get_student(student_id: int):
    try:
        with get_connection() as conn:
            cur = conn.cursor()
            cur.execute(
                "SELECT id, name, major, gpa FROM students WHERE id = ?",
                (student_id,)
            )
            return cur.fetchone()
    except sqlite3.Error as e:
        logging.error("Database error during student lookup: %s", e)
        return None


def main():
    raw_input_value = input("Enter student ID: ").strip()
    student_id = parse_student_id(raw_input_value)

    if student_id is None:
        print("Invalid student ID. Please enter a positive whole number.")
        return

    row = get_student(student_id)

    if row is None:
        print("No student found with the given ID, or an error occurred.")
    else:
        print(f"ID: {row[0]}")
        print(f"Name: {row[1]}")
        print(f"Major: {row[2]}")
        print(f"GPA: {row[3]}")


if __name__ == "__main__":
    main()
