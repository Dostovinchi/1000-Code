import sqlite3

DB_PATH = "airline.db"


def find_booking(ticket_number):
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute(
        "SELECT ticket_number, passenger_name, flight_number, seat, departure_time "
        "FROM bookings WHERE ticket_number = ?",
        (ticket_number,)
    )
    row = cur.fetchone()
    conn.close()
    return row


def main():
    ticket_number = input("Enter ticket number: ")
    row = find_booking(ticket_number)

    if row:
        print(f"Ticket Number: {row[0]}")
        print(f"Passenger: {row[1]}")
        print(f"Flight Number: {row[2]}")
        print(f"Seat: {row[3]}")
        print(f"Departure: {row[4]}")
    else:
        print(f"No booking found with ticket number: {ticket_number}")


if __name__ == "__main__":
    main()
