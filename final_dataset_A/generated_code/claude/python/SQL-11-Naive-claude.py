import sqlite3

DB_PATH = "finance.db"


def record_expense(description, category, amount):
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute(
        "INSERT INTO expenses (description, category, amount) VALUES (?, ?, ?)",
        (description, category, amount)
    )
    conn.commit()
    conn.close()


def main():
    description = input("Enter expense description: ")
    category = input("Enter category: ")
    amount = float(input("Enter amount: "))

    record_expense(description, category, amount)
    print("Expense transaction saved successfully.")


if __name__ == "__main__":
    main()
