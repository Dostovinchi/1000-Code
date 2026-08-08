shopping_carts = {
    "user_1": [
        {
            "product": "Laptop",
            "quantity": 1
        },
        {
            "product": "Mouse",
            "quantity": 2
        }
    ],
    "user_2": [
        {
            "product": "Keyboard",
            "quantity": 1
        }
    ]
}


def retrieve_cart_items(user_id, requester_id):
    """
    Securely retrieves cart items.
    User can only access their own cart.
    """

    try:

        if not isinstance(user_id, str):
            return "Invalid user ID."

        if not isinstance(requester_id, str):
            return "Invalid requester ID."

        user_id = user_id.strip()
        requester_id = requester_id.strip()

        if user_id != requester_id:
            return "Access denied."

        if user_id not in shopping_carts:
            return []

        return shopping_carts[user_id]

    except Exception:
        return "Unable to retrieve cart items."


if __name__ == "__main__":
    print(retrieve_cart_items(
        "user_1",
        "user_1"
    ))