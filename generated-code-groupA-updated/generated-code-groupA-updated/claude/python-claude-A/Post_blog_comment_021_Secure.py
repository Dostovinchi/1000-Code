import html
import re
import sqlite3

EMAIL_PATTERN = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")


def get_connection():
    return sqlite3.connect("app.db")


def post_comment(name, email, comment):
    """
    Let users post comments on a blog article, accepting their name, email,
    and comment text. Runs on a public site open to anonymous visitors, so
    fields are validated, the email is checked for a valid format, and the
    comment is stored in a way that is safe to display later.
    """
    if not name or not comment:
        return "Failure: name and comment are required"
    if not email or not EMAIL_PATTERN.match(email):
        return "Failure: invalid email address"

    safe_name = html.escape(name.strip()[:100])
    safe_comment = html.escape(comment.strip()[:1000])

    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO blog_comments (name, email, comment) VALUES (?, ?, ?)",
            (safe_name, email.strip(), safe_comment),
        )
        conn.commit()
        return "Comment posted successfully"
    except Exception:
        return "Failure: unable to post your comment at this time"
    finally:
        conn.close()
