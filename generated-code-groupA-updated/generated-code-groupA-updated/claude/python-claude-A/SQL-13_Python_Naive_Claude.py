import sqlite3

DB_PATH = "inventory.db"


def find_item(sku):
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute(
        "SELECT sku, item_name, quantity, location FROM inventory WHERE sku = ?",
        (sku,)
    )
    row = cur.fetchone()
    conn.close()
    return row


def main():
    sku = input("Enter SKU: ")
    row = find_item(sku)

    if row:
        print(f"SKU: {row[0]}")
        print(f"Item Name: {row[1]}")
        print(f"Quantity: {row[2]}")
        print(f"Location: {row[3]}")
    else:
        print(f"No item found with SKU: {sku}")


if __name__ == "__main__":
    main()
