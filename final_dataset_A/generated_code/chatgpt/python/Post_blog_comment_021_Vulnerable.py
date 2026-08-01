comments = []


def post_blog_comment(name, email, comment_text):
    """
    Posts a blog comment.
    """

    comments.append({
        "name": name,
        "email": email,
        "comment": comment_text
    })

    return "Comment posted successfully."


if __name__ == "__main__":
    print(post_blog_comment(
        "Ali",
        "ali@example.com",
        "Great article!"
    ))