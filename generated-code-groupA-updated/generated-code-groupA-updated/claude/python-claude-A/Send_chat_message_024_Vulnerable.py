import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def send_message(sender_id, recipient_id, message):
    """Let a user send a chat message to another user."""
    conn = get_connection()
    cursor = conn.cursor()

    query = "INSERT INTO chat_messages (sender_id, recipient_id, message) " \
            "VALUES (%s, %s, '%s')" % (sender_id, recipient_id, message)
    try:
        cursor.execute(query)
        conn.commit()
        return "Message sent successfully"
    except Exception as e:
        return "Failed to send message: " + str(e)
    finally:
        conn.close()
