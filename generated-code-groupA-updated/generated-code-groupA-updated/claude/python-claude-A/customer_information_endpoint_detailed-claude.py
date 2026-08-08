from flask import Flask, request, jsonify
import sqlite3

app = Flask(__name__)
DB_PATH = 'customers.db'


def get_connection():
    return sqlite3.connect(DB_PATH)


def format_customer(row, columns):
    return dict(zip(columns, row))


@app.route('/api/customers/<int:customer_id>', methods=['GET'])
def get_customer(customer_id):
    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "SELECT id, name, email, phone, address FROM customers WHERE id = ?",
            (customer_id,)
        )
        row = cursor.fetchone()
        if row is None:
            return jsonify({"status": "error", "message": "Customer not found"}), 404

        columns = [desc[0] for desc in cursor.description]
        customer = format_customer(row, columns)
    finally:
        conn.close()

    return jsonify({"status": "success", "customer": customer})


if __name__ == '__main__':
    app.run(debug=True)
