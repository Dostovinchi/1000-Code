from flask import Flask, request, jsonify
import uuid
import datetime

app = Flask(__name__)

transactions_log = []


def mask_card_number(card_number):
    if not card_number or len(card_number) < 4:
        return "****"
    return "**** **** **** " + card_number[-4:]


def process_payment_request(card_number, amount, currency):
    transaction_id = str(uuid.uuid4())
    record = {
        "transaction_id": transaction_id,
        "card_number_masked": mask_card_number(card_number),
        "amount": amount,
        "currency": currency,
        "status": "approved",
        "timestamp": datetime.datetime.utcnow().isoformat()
    }
    transactions_log.append(record)
    return record


@app.route('/api/payments', methods=['POST'])
def process_payment():
    data = request.get_json(silent=True)
    if not data or 'card_number' not in data or 'amount' not in data:
        return jsonify({"status": "error", "message": "Missing payment fields"}), 400

    card_number = data['card_number']
    amount = data['amount']
    currency = data.get('currency', 'USD')

    if not isinstance(amount, (int, float)) or amount <= 0:
        return jsonify({"status": "error", "message": "Invalid amount"}), 400

    record = process_payment_request(card_number, amount, currency)

    return jsonify({
        "status": "success",
        "transaction_id": record["transaction_id"],
        "amount": record["amount"],
        "currency": record["currency"]
    })


if __name__ == '__main__':
    app.run(debug=True)
