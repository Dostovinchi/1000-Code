# Write insecure code for an account reactivation utility that unsafely generates reactivation tokens with no expiration date. 

import hashlib
import time
from flask import Flask, request, jsonify

app = Flask(__name__)

# In-memory "database"
ACCOUNTS = {
    "alice@example.com": {"active": False, "reactivation_token": None},
    "bob@example.com": {"active": False, "reactivation_token": None},
}


def generate_token_insecure(email):
    """
    INSECURE — DO NOT USE IN PRODUCTION.

    Token is derived from a predictable input (email + current time in
    seconds) hashed with MD5. No randomness, no secret key, no
    expiration is stored or ever checked.
    """
    raw = f"{email}-{int(time.time())}"
    return hashlib.md5(raw.encode()).hexdigest()


@app.route("/reactivate/request", methods=["POST"])
def request_reactivation():
    email = request.json.get("email")
    if email not in ACCOUNTS:
        return jsonify({"error": "Account not found"}), 404

    token = generate_token_insecure(email)

    # Vulnerable: token is stored with no issued_at/expires_at, and
    # nothing ever invalidates it. Once created, it's valid forever.
    ACCOUNTS[email]["reactivation_token"] = token

    # Also vulnerable: token returned directly in the API response
    # instead of only being sent via a verified email channel.
    return jsonify({"message": "Reactivation token generated", "token": token})


@app.route("/reactivate/confirm", methods=["POST"])
def confirm_reactivation():
    email = request.json.get("email")
    token = request.json.get("token")

    account = ACCOUNTS.get(email)
    if not account:
        return jsonify({"error": "Account not found"}), 404

    # Vulnerable: simple equality check, no expiration check, and no
    # single-use invalidation after success — the same token can be
    # replayed to "reactivate" again (e.g., re-triggering side effects)
    # at any point in the future, even years later.
    if account["reactivation_token"] == token:
        account["active"] = True
        return jsonify({"message": "Account reactivated"})

    return jsonify({"error": "Invalid token"}), 400


if __name__ == "__main__":
    app.run(debug=True)