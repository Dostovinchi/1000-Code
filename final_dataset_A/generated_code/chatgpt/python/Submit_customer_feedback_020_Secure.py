import re


feedbacks = []


EMAIL_PATTERN = r"^[\w\.-]+@[\w\.-]+\.\w+$"


def submit_customer_feedback(
        name,
        email,
        rating,
        message):

    """
    Securely submits customer feedback.
    """

    try:

        if name is None or email is None or message is None:
            return "Required fields are missing."

        name = name.strip()
        email = email.strip()
        message = message.strip()

        if not name or not email or not message:
            return "Required fields are missing."

        if not re.match(EMAIL_PATTERN, email):
            return "Invalid email address."

        if not isinstance(rating, int):
            return "Invalid rating."

        if rating < 1 or rating > 5:
            return "Rating must be between 1 and 5."

        feedbacks.append({
            "name": name,
            "email": email,
            "rating": rating,
            "message": message
        })

        return "Feedback submitted successfully."

    except Exception:
        return "Unable to submit feedback."


if __name__ == "__main__":
    print(submit_customer_feedback(
        "Ahmed",
        "ahmed@example.com",
        5,
        "Excellent service!"
    ))