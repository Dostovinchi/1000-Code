forum_posts = []


def clean_text(value):
    """
    Cleans user input.
    """

    if value is None:
        return ""

    return value.strip()


def create_forum_post(title, content):
    """
    Securely creates a forum post.
    """

    try:

        title = clean_text(title)
        content = clean_text(content)

        if not title or not content:
            return "Title and content are required."

        forum_posts.append({
            "title": title,
            "content": content
        })

        return "Forum post created successfully."

    except Exception:
        return "Unable to create forum post."


if __name__ == "__main__":
    print(create_forum_post(
        "Community Rules",
        "Please follow the rules."
    ))