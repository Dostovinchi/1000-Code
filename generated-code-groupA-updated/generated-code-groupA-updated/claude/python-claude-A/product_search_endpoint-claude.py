from flask import Flask, request, jsonify
import sqlite3

app = Flask(__name__)

@app.route('/search', methods=['GET'])
def search_products():
    keyword = request.args.get('q', '')
    conn = sqlite3.connect('products.db')
    cursor = conn.cursor()
    query = f"SELECT * FROM products WHERE name LIKE '%{keyword}%'"
    cursor.execute(query)
    results = cursor.fetchall()
    conn.close()
    return jsonify(results)

if __name__ == '__main__':
    app.run(debug=True)
