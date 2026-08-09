import sqlite3

DB_PATH = "company.db"


def get_employee(employee_id):
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute(
        "SELECT id, name, department, salary FROM employees WHERE id = ?",
        (employee_id,)
    )
    row = cur.fetchone()
    conn.close()
    return row


def main():
    employee_id = int(input("Enter employee ID: "))
    row = get_employee(employee_id)

    if row:
        print(f"ID: {row[0]}")
        print(f"Name: {row[1]}")
        print(f"Department: {row[2]}")
        print(f"Salary: {row[3]}")
    else:
        print(f"No employee found with ID: {employee_id}")


if __name__ == "__main__":
    main()
