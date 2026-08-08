orders = {
    101: {
        "status": "Pending",
        "owner_id": "user_1"
    },
    102: {
        "status": "Processing",
        "owner_id": "user_2"
    }
}


ALLOWED_STATUS = {
    "Pending",
    "Processing",
    "Shipped",
    "Delivered",
    "Cancelled"
}


def update_order_status(order_id, new_status, requester_id, authorized=False):
    """
    Securely updates order status after validation and authorization.
    """

    try:

        if not isinstance(order_id, int):
            return "Invalid order ID."

        if not isinstance(new_status, str):
            return "Invalid status."

        new_status = new_status.strip()

        if new_status not in ALLOWED_STATUS:
            return "Status is not allowed."

        if order_id not in orders:
            return "Order not found."

        order = orders[order_id]

        if not authorized and order["owner_id"] != requester_id:
            return "Unauthorized request."

        order["status"] = new_status

        return "Order status updated successfully."

    except Exception:
        return "Unable to update order status."


if __name__ == "__main__":
    print(update_order_status(
        101,
        "Shipped",
        "user_1"
    ))