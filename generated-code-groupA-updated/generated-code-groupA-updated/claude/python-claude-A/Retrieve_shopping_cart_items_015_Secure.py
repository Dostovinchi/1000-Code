import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def get_cart_items(user_id, requesting_user_id):
    """
    Retrieve all shopping cart items for a user, given their user ID.
    Validates the user ID and ensures a user can only access their own cart.
    """
    if not isinstance(user_id, int) or user_id <= 0:
        return []
    if requesting_user_id != user_id:
        return []

    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "SELECT item_id, product_name, quantity, price "
            "FROM cart_items WHERE user_id = ?",
            (user_id,),
        )
        rows = cursor.fetchall()
        return rows if rows else []
    except Exception:
        return []
    finally:
        conn.close()
