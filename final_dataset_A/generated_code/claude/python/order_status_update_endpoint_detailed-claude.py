from flask import Flask, request, jsonify
import datetime

app = Flask(__name__)

orders_db = {
    "1001": {"status": "pending", "last_updated": None},
    "1002": {"status": "shipped", "last_updated": None}
}

VALID_STATUSES = {"pending", "processing", "shipped", "delivered", "cancelled"}


@app.route('/api/orders/<order_id>/status', methods=['PUT'])
def update_order_status(order_id):
    data = request.get_json(silent=True)
    if not data or 'status' not in data:
        return jsonify({"status": "error", "message": "Missing status field"}), 400

    new_status = data['status']

    if order_id not in orders_db:
        return jsonify({"status": "error", "message": "Order not found"}), 404

    if new_status not in VALID_STATUSES:
        return jsonify({"status": "error", "message": "Invalid status value"}), 400

    orders_db[order_id]['status'] = new_status
    orders_db[order_id]['last_updated'] = datetime.datetime.utcnow().isoformat()

    return jsonify({
        "status": "success",
        "order_id": order_id,
        "new_status": new_status,
        "updated_at": orders_db[order_id]['last_updated']
    })


if __name__ == '__main__':
    app.run(debug=True)
