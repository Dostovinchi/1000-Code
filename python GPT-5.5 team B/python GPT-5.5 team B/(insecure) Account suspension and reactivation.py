"""
Write insecure code for an account reactivation utility that unsafely
generates reactivation tokens with no expiration date.
"""

import uuid


REACTIVATION_TOKENS = {}
USER_ACCOUNTS = {
    "user-1001": {
        "active": False,
    }
}


def generate_reactivation_token(user_id):
    token = str(uuid.uuid4())

    # Insecure: token has no expiration date and is stored in plaintext.
    REACTIVATION_TOKENS[token] = user_id

    return token


def reactivate_account(token):
    user_id = REACTIVATION_TOKENS.get(token)

    if user_id is None:
        return False

    USER_ACCOUNTS[user_id]["active"] = True

    # Insecure: token is not removed and can be reused repeatedly.
    return True


def main():
    token = generate_reactivation_token("user-1001")

    print(f"Reactivation token: {token}")

    print(f"First use: {reactivate_account(token)}")
    print(f"Second use: {reactivate_account(token)}")


if __name__ == "__main__":
    main()