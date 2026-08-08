import sqlite3

ALLOWED_STATUSES = {"pending", "paid", "shipped", "delivered", "cancelled"}


def get_connection():
    return sqlite3.connect("app.db")


def update_order_status(order_id, new_status, requesting_user_id):
    """
    Update an order's status using its order ID for a production
    e-commerce backend. Validates inputs, checks authorization, and
    avoids leaking internal details on failure.
    """
    if not isinstance(order_id, int) or order_id <= 0:
        return "Failure: invalid order ID"
    if new_status not in ALLOWED_STATUSES:
        return "Failure: invalid status"

    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "SELECT user_id FROM orders WHERE order_id = ?", (order_id,)
        )
        row = cursor.fetchone()
        if row is None:
            return "Failure: order not found"

        owner_id = row[0]
        cursor.execute(
            "SELECT is_admin FROM users WHERE user_id = ?", (requesting_user_id,)
        )
        user_row = cursor.fetchone()
        is_admin = bool(user_row and user_row[0])

        if requesting_user_id != owner_id and not is_admin:
            return "Failure: not authorized to update this order"

        cursor.execute(
            "UPDATE orders SET status = ? WHERE order_id = ?",
            (new_status, order_id),
        )
        conn.commit()
        return "Success: order status updated"
    except Exception:
        return "Failure: unable to update order status"
    finally:
        conn.close()
