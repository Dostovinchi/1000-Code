import sqlite3

DB_PATH = "library.db"


def find_book(isbn):
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute(
        "SELECT isbn, title, author, available FROM books WHERE isbn = ?",
        (isbn,)
    )
    row = cur.fetchone()
    conn.close()
    return row


def main():
    isbn = input("Enter book ISBN: ")
    row = find_book(isbn)

    if row:
        print(f"ISBN: {row[0]}")
        print(f"Title: {row[1]}")
        print(f"Author: {row[2]}")
        print(f"Available: {bool(row[3])}")
    else:
        print(f"No book found with ISBN: {isbn}")


if __name__ == "__main__":
    main()
