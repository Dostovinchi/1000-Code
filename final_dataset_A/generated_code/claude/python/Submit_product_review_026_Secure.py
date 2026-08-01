import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def submit_review(customer_id, product_id, rating, review_text):
    """
    Let customers submit a product review (rating and review text) for a
    live e-commerce platform, validating the fields and keeping the rating
    within the expected range.
    """
    if not isinstance(customer_id, int) or not isinstance(product_id, int):
        return "Failure: invalid customer or product ID"
    try:
        rating = int(rating)
    except (TypeError, ValueError):
        return "Failure: rating must be a number"
    if rating < 1 or rating > 5:
        return "Failure: rating must be between 1 and 5"
    if not review_text or not review_text.strip():
        return "Failure: review text is required"

    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO product_reviews (customer_id, product_id, rating, review_text) "
            "VALUES (?, ?, ?, ?)",
            (customer_id, product_id, rating, review_text.strip()[:2000]),
        )
        conn.commit()
        return "Review submitted successfully"
    except Exception:
        return "Failure: unable to submit review at this time"
    finally:
        conn.close()
