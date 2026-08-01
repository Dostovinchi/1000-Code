import sqlite3
import os
import re
import logging
from contextlib import contextmanager

# Database connection settings are loaded from environment variables so
# credentials are never hardcoded in source code.
DB_PATH = os.environ.get("AIRLINE_DB_PATH", "airline.db")

# Dedicated logger that avoids recording sensitive payload data (e.g. raw
# ticket numbers are not logged; only generic event descriptions are).
logger = logging.getLogger("flight_booking_lookup")
logger.setLevel(logging.ERROR)
_handler = logging.FileHandler("app_errors.log")
_handler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(message)s"))
logger.addHandler(_handler)

TICKET_PATTERN = re.compile(r"^[A-Za-z0-9-]{6,20}$")


@contextmanager
def get_connection():
    # For a networked database, connection settings such as host, port,
    # and TLS options would also be loaded from environment variables here,
    # e.g. sslmode="require" for PostgreSQL or ssl={"ca": ...} for MySQL.
    conn = sqlite3.connect(DB_PATH)
    try:
        yield conn
    finally:
        conn.close()


def is_valid_ticket_number(value: str) -> bool:
    return bool(value) and bool(TICKET_PATTERN.match(value))


def find_booking(ticket_number: str):
    try:
        with get_connection() as conn:
            cur = conn.cursor()
            cur.execute(
                "SELECT ticket_number, passenger_name, flight_number, seat, departure_time "
                "FROM bookings WHERE ticket_number = ?",
                (ticket_number,)
            )
            return cur.fetchone()
    except sqlite3.Error:
        logger.error("Database error during flight booking lookup")
        return None


def main():
    ticket_number = input("Enter ticket number: ").strip()

    if not is_valid_ticket_number(ticket_number):
        print("Invalid ticket number format.")
        logger.warning("Rejected malformed ticket number input")
        return

    row = find_booking(ticket_number)

    if row is None:
        print("No booking found with the given ticket number, or an error occurred.")
    else:
        print(f"Ticket Number: {row[0]}")
        print(f"Passenger: {row[1]}")
        print(f"Flight Number: {row[2]}")
        print(f"Seat: {row[3]}")
        print(f"Departure: {row[4]}")


if __name__ == "__main__":
    main()
