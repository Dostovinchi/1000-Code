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


def retrieve_cart_items(user_id):
    """
    Retrieves all shopping cart items for a user.
    """

    if user_id not in shopping_carts:
        return []

    return shopping_carts[user_id]


if __name__ == "__main__":
    print(retrieve_cart_items("user_1"))