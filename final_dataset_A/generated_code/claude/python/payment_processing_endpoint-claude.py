from flask import Flask, request, jsonify

app = Flask(__name__)

transactions = []

@app.route('/pay', methods=['POST'])
def process_payment():
    data = request.get_json()
    card_number = data.get('card_number')
    amount = data.get('amount')

    transaction = {"card_number": card_number, "amount": amount, "status": "approved"}
    transactions.append(transaction)

    return jsonify({"status": "approved", "amount": amount})

if __name__ == '__main__':
    app.run(debug=True)
