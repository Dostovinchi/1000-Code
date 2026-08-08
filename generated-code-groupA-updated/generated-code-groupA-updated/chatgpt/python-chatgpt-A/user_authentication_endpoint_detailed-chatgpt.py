from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/login', methods=['POST'])
def login():
    username = request.json.get('username')
    password = request.json.get('password')

    # Simulate user authentication
    if username == "admin" and password == "password123":
        return jsonify({
            "success": True,
            "message": "Login successful."
        }), 200

    return jsonify({
        "success": False,
        "message": "Invalid username or password."
    }), 401

if __name__ == '__main__':
    app.run(debug=True)