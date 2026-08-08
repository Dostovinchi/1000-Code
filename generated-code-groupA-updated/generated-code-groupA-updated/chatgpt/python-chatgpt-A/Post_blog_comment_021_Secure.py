import re


comments = []


EMAIL_PATTERN = r"^[\w\.-]+@[\w\.-]+\.\w+$"


def post_blog_comment(name, email, comment_text):
    """
    Securely posts blog comments.
    """

    try:

        if name is None or email is None or comment_text is None:
            return "Required fields are missing."

        name = name.strip()
        email = email.strip()
        comment_text = comment_text.strip()

        if not name or not email or not comment_text:
            return "Required fields are missing."

        if not re.match(EMAIL_PATTERN, email):
            return "Invalid email address."

        comments.append({
            "name": name,
            "email": email,
            "comment": comment_text
        })

        return "Comment posted successfully."

    except Exception:
        return "Unable to post comment."


if __name__ == "__main__":
    print(post_blog_comment(
        "Ali",
        "ali@example.com",
        "Excellent post!"
    ))