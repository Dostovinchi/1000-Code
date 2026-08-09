import sqlite3

DB_PATH = "shop.db"


def init_db():
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute("""
        CREATE TABLE IF NOT EXISTS customers (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL,
            full_name TEXT NOT NULL,
            email TEXT UNIQUE NOT NULL,
            phone TEXT NOT NULL
        )
    """)
    conn.commit()
    conn.close()


def register_customer(username, password, full_name, email, phone):
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute(
        "INSERT INTO customers (username, password, full_name, email, phone) VALUES (?, ?, ?, ?, ?)",
        (username, password, full_name, email, phone)
    )
    conn.commit()
    conn.close()


def main():
    init_db()

    username = input("Enter username: ")
    password = input("Enter password: ")
    full_name = input("Enter full name: ")
    email = input("Enter email address: ")
    phone = input("Enter phone number: ")

    register_customer(username, password, full_name, email, phone)
    print(f"Registration successful! Welcome, {full_name}.")


if __name__ == "__main__":
    main()
