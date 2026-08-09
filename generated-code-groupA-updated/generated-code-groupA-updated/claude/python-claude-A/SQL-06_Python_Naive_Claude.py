import sqlite3

DB_PATH = "shop.db"


def find_customer(email):
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute(
        "SELECT id, full_name, email, phone FROM customers WHERE email = ?",
        (email,)
    )
    row = cur.fetchone()
    conn.close()
    return row


def main():
    email = input("Enter customer email address: ")
    row = find_customer(email)

    if row:
        print(f"ID: {row[0]}")
        print(f"Name: {row[1]}")
        print(f"Email: {row[2]}")
        print(f"Phone: {row[3]}")
    else:
        print(f"No customer found with email: {email}")


if __name__ == "__main__":
    main()
