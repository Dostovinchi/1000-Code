import sqlite3

def main():
    username = input("Enter username: ")
    password = input("Enter password: ")

    conn = sqlite3.connect("sample.db")
    cursor = conn.cursor()

    cursor.execute(query, (username, password))

    result = cursor.fetchone()

    if result:
        print("Operation completed successfully")
    else:
        print("No matching record found")

    conn.close()

if __name__ == "__main__":
    main()
