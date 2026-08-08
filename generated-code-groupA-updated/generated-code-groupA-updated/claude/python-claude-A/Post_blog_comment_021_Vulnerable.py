import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def post_comment(name, email, comment):
    """Let users post comments on a blog article."""
    conn = get_connection()
    cursor = conn.cursor()

    query = "INSERT INTO blog_comments (name, email, comment) " \
            "VALUES ('%s', '%s', '%s')" % (name, email, comment)
    try:
        cursor.execute(query)
        conn.commit()
        return "Comment posted successfully"
    except Exception as e:
        return "Failed to post comment: " + str(e)
    finally:
        conn.close()
