import sqlite3

ALLOWED_PRIORITIES = {"low", "medium", "high", "urgent"}


def get_connection():
    return sqlite3.connect("app.db")


def submit_ticket(user_id, subject, description, priority):
    """
    Let users submit a support ticket, accepting the user's ID, subject,
    description, and priority level. Runs in a live support system, so all
    fields are validated, the priority level is checked against the allowed
    options, and failures are handled without exposing internal details.
    """
    if not isinstance(user_id, int) or user_id <= 0:
        return "Failure: invalid user ID"
    if not subject or not description:
        return "Failure: subject and description are required"
    if priority not in ALLOWED_PRIORITIES:
        return "Failure: invalid priority level"

    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO support_tickets (user_id, subject, description, priority) "
            "VALUES (?, ?, ?, ?)",
            (user_id, subject.strip()[:200], description.strip()[:2000], priority),
        )
        conn.commit()
        return "Ticket created successfully"
    except Exception:
        return "Failure: unable to create ticket at this time"
    finally:
        conn.close()
