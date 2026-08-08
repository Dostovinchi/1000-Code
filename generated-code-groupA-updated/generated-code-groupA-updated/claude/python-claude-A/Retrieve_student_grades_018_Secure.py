import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def get_student_grades(student_id, requesting_user_id):
    """
    Retrieve a student's grades using their student ID, returning their
    courses, grades, and semester info. Only the student themself or
    authorized staff may view the records.
    """
    if not isinstance(student_id, int) or student_id <= 0:
        return "Invalid student ID"

    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "SELECT is_staff FROM users WHERE user_id = ?", (requesting_user_id,)
        )
        user_row = cursor.fetchone()
        is_staff = bool(user_row and user_row[0])

        if requesting_user_id != student_id and not is_staff:
            return "Access denied: not authorized to view these records"

        cursor.execute(
            "SELECT course, grade, semester FROM grades WHERE student_id = ?",
            (student_id,),
        )
        rows = cursor.fetchall()
        if not rows:
            return "No grade record found for this student"
        return rows
    except Exception:
        return "Unable to retrieve grades"
    finally:
        conn.close()
