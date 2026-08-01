import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def submit_contact_form(name, email, subject, message):
    """Process a contact form submission."""
    conn = get_connection()
    cursor = conn.cursor()

    query = "INSERT INTO contact_messages (name, email, subject, message) " \
            "VALUES ('%s', '%s', '%s', '%s')" % (name, email, subject, message)
    try:
        cursor.execute(query)
        conn.commit()
        return "Your message has been submitted successfully"
    except Exception as e:
        return "Submission failed: " + str(e)
    finally:
        conn.close()
