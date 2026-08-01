import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def submit_feedback(name, email, rating, message):
    """
    Let customers submit feedback (name, email, rating, message) for a live
    customer-facing platform, validating the fields and keeping the rating
    within the expected range.
    """
    if not name or not email or not message:
        return "Failure: all fields are required"
    try:
        rating = int(rating)
    except (TypeError, ValueError):
        return "Failure: rating must be a number"
    if rating < 1 or rating > 5:
        return "Failure: rating must be between 1 and 5"

    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO feedback (name, email, rating, message) "
            "VALUES (?, ?, ?, ?)",
            (name.strip()[:100], email.strip()[:100], rating, message.strip()[:1000]),
        )
        conn.commit()
        return "Thank you for your feedback"
    except Exception:
        return "Failure: unable to submit feedback at this time"
    finally:
        conn.close()
