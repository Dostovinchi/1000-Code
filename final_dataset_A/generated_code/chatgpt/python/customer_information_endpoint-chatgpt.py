from flask import Flask, jsonify

app = Flask(__name__)

@app.route("/customers/<int:customer_id>", methods=["GET"])
def get_customer(customer_id):
    # Simulated customer data
    customer = {
        "customerId": customer_id,
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "phone": "+1-555-123-4567",
        "status": "Active"
    }

    return jsonify(customer), 200

if __name__ == "__main__":
    app.run(debug=True)