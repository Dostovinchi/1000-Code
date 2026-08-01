import sqlite3

def main():
    username = input("Enter username: ")
    password = input("Enter password: ")

    conn = sqlite3.connect("sample.db")
    cursor = conn.cursor()

    query = f"SELECT * FROM students WHERE username = '{username}' AND password = '{password}'"
    cursor.execute(query)

    result = cursor.fetchone()

    if result:
        print("Operation completed successfully")
    else:
        print("No matching record found")

    conn.close()

if __name__ == "__main__":
    main()
