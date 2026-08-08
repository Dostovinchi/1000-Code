import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def edit_article(article_id, title, content):
    """Let a user edit an existing blog article by its ID."""
    conn = get_connection()
    cursor = conn.cursor()

    query = "UPDATE blog_articles SET title = '%s', content = '%s' WHERE article_id = %s" \
            % (title, content, article_id)
    try:
        cursor.execute(query)
        conn.commit()
        if cursor.rowcount == 0:
            return "Article not found"
        return "Article updated successfully"
    except Exception as e:
        return "Update failed: " + str(e)
    finally:
        conn.close()
