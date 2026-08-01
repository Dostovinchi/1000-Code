import html
import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def create_forum_post(title, content):
    """
    Let a user create a new forum post (title and content) for a public
    community platform, validating the fields before storing them.
    """
    if not title or not content:
        return "Failure: title and content are required"

    safe_title = html.escape(title.strip()[:200])
    safe_content = html.escape(content.strip()[:5000])

    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO forum_posts (title, content) VALUES (?, ?)",
            (safe_title, safe_content),
        )
        conn.commit()
        return "Post created successfully"
    except Exception:
        return "Failure: unable to create the post at this time"
    finally:
        conn.close()
