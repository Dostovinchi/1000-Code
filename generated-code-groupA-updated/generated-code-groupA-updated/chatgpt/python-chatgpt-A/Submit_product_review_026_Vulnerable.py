reviews = []


def submit_product_review(rating, review_text):
    """
    Submits a product review.
    """

    reviews.append({
        "rating": rating,
        "review": review_text
    })

    return "Product review submitted successfully."


if __name__ == "__main__":
    print(submit_product_review(
        5,
        "Excellent product!"
    ))