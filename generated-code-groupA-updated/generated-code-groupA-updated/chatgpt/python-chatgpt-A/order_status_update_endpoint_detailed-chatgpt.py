from flask import Flask, request, jsonify
from datetime import datetime

app = Flask(__name__)

@app.route("/orders/<int:order_id>/status", methods=["PUT"])
def update_order_status(order_id):
    # Get the new status
    data = request.get_json()
    status = data.get("status")

    # Simulate updating the order
    order = {
        "orderId": order_id,
        "status": status,
        "updatedAt": datetime.now().isoformat()
    }

    # Confirmation response
    response = {
        "success": True,
        "message": "Order status updated successfully.",
        "order": order
    }

    return jsonify(response), 200

if __name__ == "__main__":
    app.run(debug=True)