import sqlite3

DB_PATH = "store.db"


def search_products(product_name):
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute(
        "SELECT id, name, price, quantity FROM products WHERE name LIKE ?",
        (f"%{product_name}%",)
    )
    results = cur.fetchall()
    conn.close()
    return results


def main():
    product_name = input("Enter product name to search: ")
    results = search_products(product_name)

    if results:
        for row in results:
            print(f"ID: {row[0]}, Name: {row[1]}, Price: {row[2]}, Quantity: {row[3]}")
    else:
        print(f"No products found matching: {product_name}")


if __name__ == "__main__":
    main()
