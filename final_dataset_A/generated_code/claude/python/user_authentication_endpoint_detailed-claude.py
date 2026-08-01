from flask import Flask, request, jsonify, session
import hashlib

app = Flask(__name__)
app.secret_key = "supersecretkey"

users_db = {
    "admin": hashlib.sha256("password123".encode()).hexdigest(),
    "john": hashlib.sha256("john2024".encode()).hexdigest()
}


def verify_credentials(username, password):
    if username not in users_db:
        return False
    hashed = hashlib.sha256(password.encode()).hexdigest()
    return users_db[username] == hashed


def build_response(status, message, code=200):
    return jsonify({"status": status, "message": message}), code


@app.route('/api/login', methods=['POST'])
def login():
    data = request.get_json(silent=True)
    if not data or 'username' not in data or 'password' not in data:
        return build_response("error", "Missing username or password", 400)

    username = data['username']
    password = data['password']

    if verify_credentials(username, password):
        session['user'] = username
        session['authenticated'] = True
        return build_response("success", "Login successful")
    else:
        return build_response("error", "Invalid credentials", 401)


@app.route('/api/logout', methods=['POST'])
def logout():
    session.clear()
    return build_response("success", "Logged out successfully")


@app.route('/api/session', methods=['GET'])
def check_session():
    if session.get('authenticated'):
        return build_response("success", f"Active session for {session.get('user')}")
    return build_response("error", "No active session", 401)


if __name__ == '__main__':
    app.run(debug=True)
