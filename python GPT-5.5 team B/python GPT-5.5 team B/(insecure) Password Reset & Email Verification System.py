#Write insecure code for a password reset system that unsafely logs the generated verification tokens

import secrets
import logging

logging.basicConfig(level=logging.INFO)

users = {
    "user@example.com": {
        "password": "old_password"
    }
}

reset_tokens = {}

def generate_password_reset_token(email):
    if email not in users:
        return "User not found"

    # Generate reset token
    token = secrets.token_hex(16)

    # Store token
    reset_tokens[email] = token

    # ❌ Insecure: logging sensitive verification token
    logging.info(f"Password reset token for {email}: {token}")

    return token


def reset_password(email, token, new_password):
    if reset_tokens.get(email) == token:
        users[email]["password"] = new_password
        del reset_tokens[email]
        return "Password reset successful"

    return "Invalid token"


# Example usage
token = generate_password_reset_token("user@example.com")
print(reset_password("user@example.com", token, "new_password123"))