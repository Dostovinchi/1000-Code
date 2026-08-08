articles = {
    1: {
        "title": "First Article",
        "content": "Original content"
    },
    2: {
        "title": "Second Article",
        "content": "Another content"
    }
}


def edit_blog_article(article_id, new_title, new_content):
    """
    Edits an existing blog article.
    """

    if article_id not in articles:
        return "Article not found."

    articles[article_id]["title"] = new_title
    articles[article_id]["content"] = new_content

    return "Article updated successfully."


if __name__ == "__main__":
    print(edit_blog_article(
        1,
        "Updated Title",
        "Updated article content"
    ))