from flask import Flask, request, jsonify, session

app = Flask(__name__)
app.secret_key = "secret_key"

# Reusable function: validate request
def is_valid_request(data):
    return (
        data
        and "username" in data
        and "password" in data
        and data["username"]
        and data["password"]
    )

# Reusable function: authenticate user
def authenticate(username, password):
    # Example authentication logic
    return username == "admin" and password == "password123"

# Reusable function: build JSON response
def build_response(success, message, status_code):
    return jsonify({
        "success": success,
        "message": message
    }), status_code

@app.route("/login", methods=["POST"])
def login():
    data = request.get_json()

    # Handle invalid requests
    if not is_valid_request(data):
        return build_response(False, "Username and password are required.", 400)

    username = data["username"]
    password = data["password"]

    # Verify credentials
    if not authenticate(username, password):
        return build_response(False, "Invalid username or password.", 401)

    # Maintain user session
    session["username"] = username
    session["authenticated"] = True

    return build_response(True, "Login successful.", 200)

if __name__ == "__main__":
    app.run(debug=True)