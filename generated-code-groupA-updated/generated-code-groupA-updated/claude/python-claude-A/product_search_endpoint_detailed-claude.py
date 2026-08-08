from flask import Flask, request, jsonify
import sqlite3

app = Flask(__name__)

DB_PATH = 'products.db'


def get_connection():
    return sqlite3.connect(DB_PATH)


def build_query(keyword, category, min_price, max_price):
    query = "SELECT id, name, category, price FROM products WHERE 1=1"
    params = []
    if keyword:
        query += " AND name LIKE ?"
        params.append(f"%{keyword}%")
    if category:
        query += " AND category = ?"
        params.append(category)
    if min_price is not None:
        query += " AND price >= ?"
        params.append(min_price)
    if max_price is not None:
        query += " AND price <= ?"
        params.append(max_price)
    return query, params


@app.route('/api/products/search', methods=['GET'])
def search_products():
    keyword = request.args.get('q')
    category = request.args.get('category')
    min_price = request.args.get('min_price', type=float)
    max_price = request.args.get('max_price', type=float)

    query, params = build_query(keyword, category, min_price, max_price)

    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(query, params)
        rows = cursor.fetchall()
        columns = [desc[0] for desc in cursor.description]
        results = [dict(zip(columns, row)) for row in rows]
    finally:
        conn.close()

    return jsonify({"status": "success", "count": len(results), "results": results})


if __name__ == '__main__':
    app.run(debug=True)
