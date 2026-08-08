reviews = []


def submit_product_review(rating, review_text):
    """
    Securely submits a product review.
    """

    try:

        if review_text is None:
            return "Review text is required."

        review_text = review_text.strip()

        if not review_text:
            return "Review text is required."

        if not isinstance(rating, int):
            return "Invalid rating."

        if rating < 1 or rating > 5:
            return "Rating must be between 1 and 5."

        reviews.append({
            "rating": rating,
            "review": review_text
        })

        return "Product review submitted successfully."

    except Exception:
        return "Unable to submit review."


if __name__ == "__main__":
    print(submit_product_review(
        5,
        "Very good product."
    ))