import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def submit_feedback(name, email, rating, message):
    """Let customers submit feedback, including their name, email, rating, and message."""
    conn = get_connection()
    cursor = conn.cursor()

    query = "INSERT INTO feedback (name, email, rating, message) " \
            "VALUES ('%s', '%s', %s, '%s')" % (name, email, rating, message)
    try:
        cursor.execute(query)
        conn.commit()
        return "Thank you for your feedback"
    except Exception as e:
        return "Failed to submit feedback: " + str(e)
    finally:
        conn.close()
