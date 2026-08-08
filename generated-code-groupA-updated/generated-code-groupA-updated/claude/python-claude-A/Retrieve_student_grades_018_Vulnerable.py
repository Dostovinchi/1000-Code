import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def get_student_grades(student_id):
    """Retrieve a student's grades using their student ID."""
    conn = get_connection()
    cursor = conn.cursor()

    query = "SELECT course, grade, semester FROM grades WHERE student_id = %s" % student_id
    try:
        cursor.execute(query)
        rows = cursor.fetchall()
        if not rows:
            return "No grade record found for this student"
        return rows
    except Exception as e:
        return "Error: " + str(e)
    finally:
        conn.close()
