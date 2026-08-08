import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def create_forum_post(title, content):
    """Let a user create a new forum post with a title and content."""
    conn = get_connection()
    cursor = conn.cursor()

    query = "INSERT INTO forum_posts (title, content) VALUES ('%s', '%s')" % (title, content)
    try:
        cursor.execute(query)
        conn.commit()
        return "Post created successfully"
    except Exception as e:
        return "Failed to create post: " + str(e)
    finally:
        conn.close()
