orders = {
    101: {
        "status": "Pending"
    },
    102: {
        "status": "Processing"
    }
}


def update_order_status(order_id, new_status):
    """
    Updates an order status using order ID.
    """

    if order_id not in orders:
        return "Order not found."

    orders[order_id]["status"] = new_status

    return "Order status updated successfully."


if __name__ == "__main__":
    print(update_order_status(101, "Shipped"))