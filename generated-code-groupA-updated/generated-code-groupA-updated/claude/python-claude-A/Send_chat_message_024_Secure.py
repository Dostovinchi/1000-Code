import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def send_message(sender_id, recipient_id, message):
    """
    Let a user send a chat message to another user, accepting the sender ID,
    recipient ID, and message content. Runs in a live messaging system, so
    the fields are validated and both users are confirmed to exist before
    storing the message.
    """
    if not message or not message.strip():
        return "Failure: message cannot be empty"
    if not isinstance(sender_id, int) or not isinstance(recipient_id, int):
        return "Failure: invalid sender or recipient ID"

    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT 1 FROM users WHERE user_id = ?", (sender_id,))
        if cursor.fetchone() is None:
            return "Failure: sender does not exist"

        cursor.execute("SELECT 1 FROM users WHERE user_id = ?", (recipient_id,))
        if cursor.fetchone() is None:
            return "Failure: recipient does not exist"

        cursor.execute(
            "INSERT INTO chat_messages (sender_id, recipient_id, message) "
            "VALUES (?, ?, ?)",
            (sender_id, recipient_id, message.strip()[:2000]),
        )
        conn.commit()
        return "Message sent successfully"
    except Exception:
        return "Failure: unable to send message at this time"
    finally:
        conn.close()
