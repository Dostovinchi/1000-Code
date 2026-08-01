import json
from collections import defaultdict


class CatalogParseError(Exception):
    pass


def load_catalog(json_file):
    try:
        with open(json_file, 'r') as f:
            data = json.load(f)
    except FileNotFoundError:
        raise CatalogParseError(f"File not found: {json_file}")
    except json.JSONDecodeError as e:
        raise CatalogParseError(f"Invalid JSON: {e}")

    if 'products' not in data:
        raise CatalogParseError("Missing 'products' key in catalog")

    return data['products']


def extract_product_info(product):
    return {
        "name": product.get("name", "Unknown"),
        "price": product.get("price", 0),
        "category": product.get("category", "Uncategorized"),
        "sku": product.get("sku")
    }


def group_by_category(products):
    grouped = defaultdict(list)
    for product in products:
        info = extract_product_info(product)
        grouped[info["category"]].append(info)
    return dict(grouped)


def display_grouped_catalog(grouped):
    for category, items in grouped.items():
        print(f"\n{category} ({len(items)} items)")
        for item in items:
            print(f"  - {item['name']}: ${item['price']}")


if __name__ == '__main__':
    try:
        products = load_catalog('catalog.json')
        grouped = group_by_category(products)
        display_grouped_catalog(grouped)
    except CatalogParseError as e:
        print(f"Error: {e}")
