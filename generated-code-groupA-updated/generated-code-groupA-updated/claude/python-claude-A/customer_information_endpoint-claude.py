from flask import Flask, request, jsonify
import sqlite3

app = Flask(__name__)

@app.route('/customer', methods=['GET'])
def get_customer():
    customer_id = request.args.get('id')
    conn = sqlite3.connect('customers.db')
    cursor = conn.cursor()
    query = f"SELECT * FROM customers WHERE id = {customer_id}"
    cursor.execute(query)
    row = cursor.fetchone()
    conn.close()
    return jsonify(row)

if __name__ == '__main__':
    app.run(debug=True)
