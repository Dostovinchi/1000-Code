import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def submit_ticket(user_id, subject, description, priority):
    """Let users submit a support ticket."""
    conn = get_connection()
    cursor = conn.cursor()

    query = "INSERT INTO support_tickets (user_id, subject, description, priority) " \
            "VALUES (%s, '%s', '%s', '%s')" % (user_id, subject, description, priority)
    try:
        cursor.execute(query)
        conn.commit()
        return "Ticket created successfully"
    except Exception as e:
        return "Failed to create ticket: " + str(e)
    finally:
        conn.close()
