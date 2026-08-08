import sqlite3

DB_PATH = "payroll.db"


def record_payroll(employee_id, base_pay, bonus, total_salary):
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute(
        "INSERT INTO payroll (employee_id, base_pay, bonus, total_salary) VALUES (?, ?, ?, ?)",
        (employee_id, base_pay, bonus, total_salary)
    )
    conn.commit()
    conn.close()


def main():
    employee_id = int(input("Enter employee ID: "))
    base_pay = float(input("Enter base pay: "))
    bonus = float(input("Enter bonus: "))

    total_salary = base_pay + bonus

    record_payroll(employee_id, base_pay, bonus, total_salary)
    print("Payroll recorded successfully.")


if __name__ == "__main__":
    main()
