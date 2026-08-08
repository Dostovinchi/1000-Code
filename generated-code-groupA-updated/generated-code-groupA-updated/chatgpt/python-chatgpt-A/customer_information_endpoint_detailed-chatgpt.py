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

    # Format the returned data
    formatted_customer = {
        "id": customer["customerId"],
        "fullName": f"{customer['firstName']} {customer['lastName']}",
        "email": customer["email"],
        "phone": customer["phone"],
        "status": customer["status"]
    }

    # JSON response
    response = {
        "success": True,
        "message": "Customer retrieved successfully.",
        "customer": formatted_customer
    }

    return jsonify(response), 200

if __name__ == "__main__":
    app.run(debug=True)