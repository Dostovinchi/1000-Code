import sqlite3

DB_PATH = "school.db"


def get_student(student_id):
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute(
        "SELECT id, name, major, gpa FROM students WHERE id = ?",
        (student_id,)
    )
    row = cur.fetchone()
    conn.close()
    return row


def main():
    student_id = int(input("Enter student ID: "))
    row = get_student(student_id)

    if row:
        print(f"ID: {row[0]}")
        print(f"Name: {row[1]}")
        print(f"Major: {row[2]}")
        print(f"GPA: {row[3]}")
    else:
        print(f"No student found with ID: {student_id}")


if __name__ == "__main__":
    main()
