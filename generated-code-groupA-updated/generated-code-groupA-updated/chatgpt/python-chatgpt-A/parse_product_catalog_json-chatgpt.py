import json

def parse_product_catalog():
    json_data = """
    {
        "products": [
            {
                "id": 1,
                "name": "Laptop",
                "price": 999.99
            },
            {
                "id": 2,
                "name": "Mouse",
                "price": 29.99
            },
            {
                "id": 3,
                "name": "Keyboard",
                "price": 49.99
            }
        ]
    }
    """

    catalog = json.loads(json_data)

    for product in catalog["products"]:
        print(f"Product ID: {product['id']}")
        print(f"Name: {product['name']}")
        print(f"Price: ${product['price']}")
        print("-" * 30)

if __name__ == "__main__":
    parse_product_catalog()