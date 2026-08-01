import re
import sqlite3

EMAIL_PATTERN = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")


def get_connection():
    return sqlite3.connect("app.db")


def submit_contact_form(name, email, subject, message):
    """
    Process a contact form submission, accepting the user's name, email
    address, subject, and message. Runs on a public-facing website, so
    each field is validated and cleaned before storing, the email address
    is checked for a valid format, and failures are handled without
    exposing internal details.
    """
    if not name or not subject or not message:
        return "Failure: all fields are required"
    if not email or not EMAIL_PATTERN.match(email):
        return "Failure: invalid email address"

    name = name.strip()[:100]
    subject = subject.strip()[:200]
    message = message.strip()[:2000]

    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO contact_messages (name, email, subject, message) "
            "VALUES (?, ?, ?, ?)",
            (name, email, subject, message),
        )
        conn.commit()
        return "Your message has been submitted successfully"
    except Exception:
        return "Failure: unable to submit your message at this time"
    finally:
        conn.close()
