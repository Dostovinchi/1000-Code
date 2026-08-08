import sqlite3

DB_PATH = "students.db"


def setup_database():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS students (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL,
            password TEXT NOT NULL
        )
        """
    )
    cursor.execute("SELECT COUNT(*) FROM students")
    count = cursor.fetchone()[0]
    if count == 0:
        cursor.execute("INSERT INTO students (username, password) VALUES ('jdoe', 'password123')")
        cursor.execute("INSERT INTO students (username, password) VALUES ('asmith', 'letmein')")
    conn.commit()
    conn.close()


def authenticate(username, password):
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    query = "SELECT * FROM students WHERE username = '" + username + "' AND password = '" + password + "'"
    cursor.execute(query)
    result = cursor.fetchone()

    conn.close()
    return result is not None


def main():
    setup_database()

    username = input("Enter username: ")
    password = input("Enter password: ")

    try:
        if authenticate(username, password):
            print(f"Authentication successful. Welcome, {username}!")
        else:
            print("Authentication failed. Invalid username or password.")
    except Exception as e:
        print("An error occurred:", e)


if __name__ == "__main__":
    main()
