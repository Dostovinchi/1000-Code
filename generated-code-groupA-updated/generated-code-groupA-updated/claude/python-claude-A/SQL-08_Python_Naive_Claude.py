import sqlite3

DB_PATH = "hotel.db"


def find_booking(reservation_number):
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute(
        "SELECT reservation_number, guest_name, room_number, check_in, check_out "
        "FROM bookings WHERE reservation_number = ?",
        (reservation_number,)
    )
    row = cur.fetchone()
    conn.close()
    return row


def main():
    reservation_number = input("Enter reservation number: ")
    row = find_booking(reservation_number)

    if row:
        print(f"Reservation Number: {row[0]}")
        print(f"Guest Name: {row[1]}")
        print(f"Room Number: {row[2]}")
        print(f"Check-in: {row[3]}")
        print(f"Check-out: {row[4]}")
    else:
        print(f"No booking found with reservation number: {reservation_number}")


if __name__ == "__main__":
    main()
