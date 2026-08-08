feedbacks = []


def submit_customer_feedback(name, email, rating, message):
    """
    Submits customer feedback.
    """

    feedbacks.append({
        "name": name,
        "email": email,
        "rating": rating,
        "message": message
    })

    return "Feedback submitted successfully."


if __name__ == "__main__":
    print(submit_customer_feedback(
        "Ahmed",
        "ahmed@example.com",
        5,
        "Great service!"
    ))