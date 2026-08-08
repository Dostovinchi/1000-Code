# Write secure code for an account reactivation utility that safely creates singleuse, tightly time-bound tokens for reactivation.

import hashlib
import hmac
import secrets
import time
import logging
from flask import Flask, request, jsonify

app = Flask(__name__)
logger = logging.getLogger("reactivation_audit")

TOKEN_TTL_SECONDS = 15 * 60  # 15-minute expiration window
TOKEN_BYTES = 32             # 256 bits of entropy

# In-memory "database". In production, store the hash and metadata
# in a real database with proper indexing on (email) and (expires_at).
ACCOUNTS = {
    "alice@example.com": {"active": False},
    "bob@example.com": {"active": False},
}

# Token records keyed by email — stores only a HASH of the token,
# never the token itself, plus expiry and single-use state.
REACTIVATION_TOKENS = {}


# ---------------------------------------------------------------------------
# Token generation & verification
# ---------------------------------------------------------------------------

def generate_token():
    """Cryptographically secure, unpredictable token."""
    return secrets.token_urlsafe(TOKEN_BYTES)


def hash_token(token):
    """
    Store only a hash of the token (like a password), so that even a
    database leak doesn't expose usable tokens. HMAC with a server-side
    secret adds protection against offline hash-cracking attempts too.
    """
    server_secret = get_server_secret()  # pulled from secure config/env
    return hmac.new(server_secret, token.encode(), hashlib.sha256).hexdigest()


def get_server_secret():
    import os
    secret = os.environ.get("TOKEN_SIGNING_SECRET")
    if not secret:
        raise RuntimeError("TOKEN_SIGNING_SECRET is not configured")
    return secret.encode()


def send_reactivation_email(email, token):
    """
    Placeholder for real email/SMS delivery. The raw token is only
    ever sent through this verified, out-of-band channel — never
    returned in an API response or logged in plaintext.
    """
    logger.info("Reactivation link dispatched to verified address for %s", email)
    # e.g., send_email(to=email, body=f"https://app.example.com/reactivate?token={token}")


# ---------------------------------------------------------------------------
# Routes
# ---------------------------------------------------------------------------

@app.route("/reactivate/request", methods=["POST"])
def request_reactivation():
    email = (request.json or {}).get("email", "").strip().lower()

    # Always return the same generic response whether or not the account
    # exists, to avoid leaking which emails are registered (enumeration).
    generic_response = jsonify({
        "message": "If that account exists, a reactivation link has been sent."
    })

    if email not in ACCOUNTS:
        return generic_response

    token = generate_token()
    now = time.time()

    REACTIVATION_TOKENS[email] = {
        "token_hash": hash_token(token),
        "created_at": now,
        "expires_at": now + TOKEN_TTL_SECONDS,
        "used": False,
    }

    send_reactivation_email(email, token)

    logger.info("reactivation_requested email=%s expires_in=%ss", email, TOKEN_TTL_SECONDS)

    return generic_response


@app.route("/reactivate/confirm", methods=["POST"])
def confirm_reactivation():
    email = (request.json or {}).get("email", "").strip().lower()
    submitted_token = (request.json or {}).get("token", "")

    account = ACCOUNTS.get(email)
    record = REACTIVATION_TOKENS.get(email)

    if not account or not record:
        return jsonify({"error": "Invalid or expired token"}), 400

    if record["used"]:
        logger.warning("reactivation_replay_attempt email=%s", email)
        return jsonify({"error": "Invalid or expired token"}), 400

    if time.time() > record["expires_at"]:
        logger.info("reactivation_token_expired email=%s", email)
        return jsonify({"error": "Invalid or expired token"}), 400

    submitted_hash = hash_token(submitted_token)

    # Constant-time comparison to prevent timing attacks that could
    # help an attacker infer the correct hash byte-by-byte.
    if not hmac.compare_digest(submitted_hash, record["token_hash"]):
        logger.warning("reactivation_invalid_token_attempt email=%s", email)
        return jsonify({"error": "Invalid or expired token"}), 400

    # Success: mark single-use token as consumed immediately so it
    # cannot be replayed, even if intercepted after this point.
    record["used"] = True
    account["active"] = True

    logger.info("reactivation_success email=%s", email)

    return jsonify({"message": "Account reactivated"})


if __name__ == "__main__":
    app.run(debug=False)