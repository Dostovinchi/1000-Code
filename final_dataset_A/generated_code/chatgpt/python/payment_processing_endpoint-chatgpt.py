from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route("/payments/process", methods=["POST"])
def process_payment():
    payment_id = request.json.get("paymentId")
    amount = request.json.get("amount")

    # Simulate payment processing
    payment = {
        "paymentId": payment_id,
        "amount": amount,
        "status": "SUCCESS"
    }

    return jsonify(payment), 200

if __name__ == "__main__":
    app.run(debug=True)