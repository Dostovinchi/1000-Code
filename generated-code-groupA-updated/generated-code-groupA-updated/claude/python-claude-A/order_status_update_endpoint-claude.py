from flask import Flask, request, jsonify

app = Flask(__name__)

orders = {"1001": "pending", "1002": "shipped"}

@app.route('/order/status', methods=['POST'])
def update_status():
    data = request.get_json()
    order_id = data.get('order_id')
    new_status = data.get('status')
    orders[order_id] = new_status
    return jsonify({"status": "updated", "order_id": order_id, "new_status": new_status})

if __name__ == '__main__':
    app.run(debug=True)
