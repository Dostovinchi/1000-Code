from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route("/orders/<int:order_id>/status", methods=["PUT"])
def update_order_status(order_id):
    status = request.json.get("status")

    # Simulate updating the order status
    order = {
        "orderId": order_id,
        "status": status
    }

    return jsonify({
        "success": True,
        "message": "Order status updated successfully.",
        "order": order
    }), 200

if __name__ == "__main__":
    app.run(debug=True)