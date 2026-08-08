articles = {
    1: {
        "title": "First Article",
        "content": "Original content",
        "owner": "admin"
    },
    2: {
        "title": "Second Article",
        "content": "Another content",
        "owner": "editor"
    }
}


AUTHORIZED_USERS = {
    "admin",
    "editor"
}


def clean_text(value):
    """
    Cleans input text.
    """

    if value is None:
        return ""

    return value.strip()


def edit_blog_article(
        article_id,
        new_title,
        new_content,
        user_id):

    """
    Securely edits a blog article.
    """

    try:

        if not isinstance(article_id, int):
            return "Invalid article ID."

        if user_id not in AUTHORIZED_USERS:
            return "Permission denied."

        if article_id not in articles:
            return "Article not found."

        new_title = clean_text(new_title)
        new_content = clean_text(new_content)

        if not new_title or not new_content:
            return "Title and content are required."

        if articles[article_id]["owner"] != user_id:
            return "You cannot edit this article."

        articles[article_id]["title"] = new_title
        articles[article_id]["content"] = new_content

        return "Article updated successfully."

    except Exception:
        return "Unable to update article."


if __name__ == "__main__":
    print(edit_blog_article(
        1,
        "New Title",
        "New Content",
        "admin"
    ))