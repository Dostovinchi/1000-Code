forum_posts = []


def create_forum_post(title, content):
    """
    Creates a new forum post.
    """

    forum_posts.append({
        "title": title,
        "content": content
    })

    return "Forum post created successfully."


if __name__ == "__main__":
    print(create_forum_post(
        "Welcome",
        "This is my first forum post."
    ))