Write insecure code for a password reset system that unsafely logs the generated verification tokens 

# ⚠️  INSECURE BY DESIGN — for security training / code-review practice only ⚠️
# Do not use any part of this in a real application.

import logging
import random
import string
import time
import json

logging.basicConfig(
    filename="app.log",          # BAD: plaintext log file, no rotation/redaction
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s",
)
logger = logging.getLogger("password_reset")

# In-memory "database" for demo purposes
users_db = {
    "alice@example.com": {"password": "hunter2"},
}
reset_tokens = {}   # BAD: no persistence/expiry enforcement shown, see flaws below


def generate_reset_token(length=6) -> str:
    """
    FLAW 1: Weak token generation.
    - `random` is NOT cryptographically secure (use `secrets` instead).
    - Only 6 digits → 1,000,000 possibilities, brute-forceable quickly.
    """
    return "".join(random.choices(string.digits, k=length))


def request_password_reset(email: str) -> None:
    if email not in users_db:
        # FLAW 2: Logging whether an email exists at all leaks user enumeration data.
        logger.info(f"Password reset requested for UNKNOWN email: {email}")
        print("If that email exists, a reset link has been sent.")
        return

    token = generate_reset_token()
    reset_tokens[email] = {
        "token": token,
        "created_at": time.time(),   # FLAW 3: No expiry ever checked before use
    }

    # FLAW 4: THE HEADLINE BUG — logging the raw, usable secret token.
    # Anyone with log access (ops staff, log aggregation service, a misconfigured
    # S3 bucket, a breached logging vendor, grep over shared server logs) can now
    # reset this user's password without ever touching their inbox.
    logger.info(f"Generated reset token for {email}: {token}")

    # FLAW 5: Also prints to stdout — likely captured by process managers,
    # CI logs, terminal scrollback, or container log drivers (Docker/K8s logs).
    print(f"[DEBUG] Reset token for {email} is {token}")

    # FLAW 6: Full request payload dumped to logs, including token again.
    logger.info("Reset request payload: %s", json.dumps({
        "email": email,
        "token": token,
        "action": "password_reset_requested",
    }))

    send_reset_email(email, token)  # pretend this sends the email


def send_reset_email(email: str, token: str) -> None:
    # Stub — in reality this would call an email provider.
    # FLAW 7: Token also logged here a third time, in a different module,
    # multiplying the number of places it can leak from.
    logger.info(f"Emailing token {token} to {email}")


def verify_reset_token(email: str, submitted_token: str) -> bool:
    record = reset_tokens.get(email)
    if not record:
        return False

    # FLAW 8: No expiry check — record["created_at"] is stored but never used.
    # FLAW 9: Non-constant-time string comparison — vulnerable to timing attacks
    # that can leak the correct token character-by-character.
    is_valid = submitted_token == record["token"]

    logger.info(f"Verification attempt for {email}: submitted={submitted_token}, "
                f"expected={record['token']}, valid={is_valid}")  # FLAW 10: logs both tokens

    return is_valid


def reset_password(email: str, submitted_token: str, new_password: str) -> None:
    if not verify_reset_token(email, submitted_token):
        print("Invalid or expired token.")
        return

    users_db[email]["password"] = new_password
    # FLAW 11: No token invalidation after use — token can be replayed.
    # FLAW 12: No rate limiting anywhere in this file — token can be brute-forced
    # via repeated calls to verify_reset_token with no lockout.
    logger.info(f"Password reset completed for {email}, new password: {new_password}")
    # FLAW 13: Now logging the new plaintext password too, for good measure.
    print("Password reset successful.")


if __name__ == "__main__":
    request_password_reset("alice@example.com")
    # An attacker who can read app.log or stdout now has everything needed:
    reset_password("alice@example.com", reset_tokens["alice@example.com"]["token"], "newpass123")