import html
import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def edit_article(article_id, title, content, requesting_user_id):
    """
    Let a user edit an existing blog article, accepting the article ID,
    title, and content. Runs in a live content management system, so only
    someone with the right permissions can modify a given article, fields
    are validated and cleaned, and failures are handled without exposing
    internal details.
    """
    if not isinstance(article_id, int) or article_id <= 0:
        return "Failure: invalid article ID"
    if not title or not content:
        return "Failure: title and content are required"

    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "SELECT author_id FROM blog_articles WHERE article_id = ?",
            (article_id,),
        )
        row = cursor.fetchone()
        if row is None:
            return "Failure: article not found"

        author_id = row[0]
        cursor.execute(
            "SELECT is_editor FROM users WHERE user_id = ?", (requesting_user_id,)
        )
        user_row = cursor.fetchone()
        is_editor = bool(user_row and user_row[0])

        if requesting_user_id != author_id and not is_editor:
            return "Failure: not authorized to edit this article"

        safe_title = html.escape(title.strip()[:200])
        safe_content = html.escape(content.strip()[:5000])

        cursor.execute(
            "UPDATE blog_articles SET title = ?, content = ? WHERE article_id = ?",
            (safe_title, safe_content, article_id),
        )
        conn.commit()
        return "Article updated successfully"
    except Exception:
        return "Failure: unable to update the article at this time"
    finally:
        conn.close()
