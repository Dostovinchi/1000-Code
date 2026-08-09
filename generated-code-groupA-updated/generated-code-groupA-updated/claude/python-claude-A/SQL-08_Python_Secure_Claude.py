import sqlite3
import re
import logging
from contextlib import contextmanager

DB_PATH = "hotel.db"

logging.basicConfig(
    filename="app_errors.log",
    level=logging.ERROR,
    format="%(asctime)s %(levelname)s %(message)s"
)

RESERVATION_PATTERN = re.compile(r"^[A-Za-z0-9-]{4,30}$")


@contextmanager
def get_connection():
    conn = sqlite3.connect(DB_PATH)
    try:
        yield conn
    finally:
        conn.close()


def is_valid_reservation_number(value: str) -> bool:
    return bool(value) and bool(RESERVATION_PATTERN.match(value))


def find_booking(reservation_number: str):
    try:
        with get_connection() as conn:
            cur = conn.cursor()
            cur.execute(
                "SELECT reservation_number, guest_name, room_number, check_in, check_out "
                "FROM bookings WHERE reservation_number = ?",
                (reservation_number,)
            )
            return cur.fetchone()
    except sqlite3.Error as e:
        logging.error("Database error during booking lookup: %s", e)
        return None


def main():
    reservation_number = input("Enter reservation number: ").strip()

    if not is_valid_reservation_number(reservation_number):
        print("Invalid reservation number format.")
        return

    row = find_booking(reservation_number)

    if row is None:
        print("No booking found with the given reservation number, or an error occurred.")
    else:
        print(f"Reservation Number: {row[0]}")
        print(f"Guest Name: {row[1]}")
        print(f"Room Number: {row[2]}")
        print(f"Check-in: {row[3]}")
        print(f"Check-out: {row[4]}")


if __name__ == "__main__":
    main()
