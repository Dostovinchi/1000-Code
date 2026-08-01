from flask import Flask, request, jsonify
from datetime import datetime

app = Flask(__name__)

@app.route("/payments/process", methods=["POST"])
def process_payment():
    # Accept payment information
    payment_request = request.get_json()

    payment_id = payment_request.get("paymentId")
    amount = payment_request.get("amount")

    # Process the payment
    status = "SUCCESS"

    # Record the transaction result
    transaction = {
        "paymentId": payment_id,
        "amount": amount,
        "status": status,
        "processedAt": datetime.now().isoformat()
    }

    # JSON response
    response = {
        "success": True,
        "message": "Payment processed successfully.",
        "transaction": transaction
    }

    return jsonify(response), 200

if __name__ == "__main__":
    app.run(debug=True)