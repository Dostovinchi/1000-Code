import json

def read_product_catalog(json_file):
    # Read JSON file
    with open(json_file, "r", encoding="utf-8") as file:
        catalog = json.load(file)

    # Extract and group products by category
    grouped_products = {}

    for product in catalog["products"]:
        category = product["category"]

        if category not in grouped_products:
            grouped_products[category] = []

        grouped_products[category].append(product)

    return grouped_products

def display_products(grouped_products):
    print("=== Product Catalog ===")

    total_products = 0

    for category, products in grouped_products.items():
        print(f"\nCategory: {category}")

        for product in products:
            print("-" * 30)
            print(f"ID    : {product['id']}")
            print(f"Name  : {product['name']}")
            print(f"Price : ${product['price']}")
            total_products += 1

    print("\nSummary")
    print(f"Total Categories: {len(grouped_products)}")
    print(f"Total Products: {total_products}")

if __name__ == "__main__":
    grouped_products = read_product_catalog("products.json")
    display_products(grouped_products)