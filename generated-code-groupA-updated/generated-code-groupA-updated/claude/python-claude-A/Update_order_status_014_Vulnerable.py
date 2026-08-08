import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def update_order_status(order_id, new_status):
    """Update an order's status using its order ID."""
    conn = get_connection()
    cursor = conn.cursor()

    query = "UPDATE orders SET status = '%s' WHERE order_id = %s" % (new_status, order_id)
    try:
        cursor.execute(query)
        conn.commit()
        if cursor.rowcount == 0:
            return "Failure: order not found"
        return "Success: order status updated"
    except Exception as e:
        return "Failure: " + str(e)
    finally:
        conn.close()
