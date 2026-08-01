import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def get_cart_items(user_id):
    """Retrieve all shopping cart items for a user, given their user ID."""
    conn = get_connection()
    cursor = conn.cursor()

    query = "SELECT item_id, product_name, quantity, price FROM cart_items WHERE user_id = %s" % user_id
    try:
        cursor.execute(query)
        rows = cursor.fetchall()
        return rows if rows else []
    except Exception:
        return []
    finally:
        conn.close()
