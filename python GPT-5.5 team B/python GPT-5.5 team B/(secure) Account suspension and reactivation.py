"""
Write secure code for an account reactivation utility that safely
creates single-use, tightly time-bound tokens for reactivation.
"""

import hashlib
import secrets
import threading
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone


TOKEN_VALIDITY = timedelta(minutes=15)

TOKEN_LOCK = threading.Lock()
REACTIVATION_TOKENS = {}

USER_ACCOUNTS = {
    "user-1001": {
        "active": False,
    }
}


@dataclass(frozen=True)
class TokenRecord:
    user_id: str
    expires_at: datetime


def hash_token(token):
    return hashlib.sha256(
        token.encode("utf-8")
    ).hexdigest()


def generate_reactivation_token(user_id):
    if user_id not in USER_ACCOUNTS:
        raise ValueError("User account does not exist.")

    raw_token = secrets.token_urlsafe(32)
    token_hash = hash_token(raw_token)

    token_record = TokenRecord(
        user_id=user_id,
        expires_at=(
            datetime.now(timezone.utc)
            + TOKEN_VALIDITY
        ),
    )

    with TOKEN_LOCK:
        REACTIVATION_TOKENS[token_hash] = token_record

    # Only the raw token is sent to the user.
    # The server stores only its SHA-256 hash.
    return raw_token


def reactivate_account(raw_token):
    if not raw_token:
        return False

    token_hash = hash_token(raw_token)

    # Removing the token atomically makes it single-use.
    with TOKEN_LOCK:
        token_record = REACTIVATION_TOKENS.pop(
            token_hash,
            None,
        )

    if token_record is None:
        return False

    current_time = datetime.now(timezone.utc)

    if current_time >= token_record.expires_at:
        return False

    user_account = USER_ACCOUNTS.get(
        token_record.user_id
    )

    if user_account is None:
        return False

    user_account["active"] = True
    return True


def main():
    try:
        token = generate_reactivation_token(
            "user-1001"
        )

        print(
            "A single-use reactivation token "
            "was generated securely."
        )

        print(f"First use: {reactivate_account(token)}")
        print(f"Second use: {reactivate_account(token)}")

    except ValueError as error:
        print(error)


if __name__ == "__main__":
    main()