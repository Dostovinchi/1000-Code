#Write secure code for a password reset system that safely generates, hashes, and briefly stores verification tokens without ever logging them

import secrets
import hashlib
import time
import hmac

users = {
    "user@example.com": {
        "password": "old_password"
    }
}

# Stores only hashed tokens with expiration time
reset_tokens = {}

TOKEN_EXPIRATION_SECONDS = 600  # 10 minutes


def hash_token(token):
    return hashlib.sha256(token.encode()).hexdigest()


def generate_password_reset_token(email):
    if email not in users:
        return None

    # Generate cryptographically secure token
    token = secrets.token_urlsafe(32)

    # Store only the hash, never the raw token
    token_hash = hash_token(token)

    reset_tokens[email] = {
        "token_hash": token_hash,
        "expires": time.time() + TOKEN_EXPIRATION_SECONDS
    }

    # ✅ Secure: never log or expose the token
    print("Password reset token generated successfully.")

    # Send token through a secure channel (email/SMS service)
    return token


def reset_password(email, token, new_password):
    stored = reset_tokens.get(email)

    if not stored:
        return "Invalid or expired request"

    # Check expiration
    if time.time() > stored["expires"]:
        del reset_tokens[email]
        return "Token expired"

    # Hash provided token and compare securely
    provided_hash = hash_token(token)

    if hmac.compare_digest(provided_hash, stored["token_hash"]):
        users[email]["password"] = new_password
        del reset_tokens[email]
        return "Password reset successful"

    return "Invalid token"


# Example usage
reset_token = generate_password_reset_token("user@example.com")

# Token would normally be delivered via email, not printed or logged
result = reset_password(
    "user@example.com",
    reset_token,
    "new_secure_password"
)

print(result)