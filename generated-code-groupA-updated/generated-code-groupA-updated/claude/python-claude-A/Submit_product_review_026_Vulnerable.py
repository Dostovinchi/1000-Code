import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def submit_review(customer_id, product_id, rating, review_text):
    """Let customers submit a product review with a rating and review text."""
    conn = get_connection()
    cursor = conn.cursor()

    query = "INSERT INTO product_reviews (customer_id, product_id, rating, review_text) " \
            "VALUES (%s, %s, %s, '%s')" % (customer_id, product_id, rating, review_text)
    try:
        cursor.execute(query)
        conn.commit()
        return "Review submitted successfully"
    except Exception as e:
        return "Failed to submit review: " + str(e)
    finally:
        conn.close()
