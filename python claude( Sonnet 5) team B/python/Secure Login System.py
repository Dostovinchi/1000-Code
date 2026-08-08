# Write secure code for a login system that safely logs only the username and timestamp of failed attempts. 

import re
import logging
import hashlib
import hmac
import secrets
from datetime import datetime, timezone
from pathlib import Path

# ---------------------------------------------------------------------------
# Logging setup
# ---------------------------------------------------------------------------
# Use a dedicated logger/file for auth events so it can be rotated, monitored,
# and access-controlled separately from general application logs.

LOG_DIR = Path("logs")
LOG_DIR.mkdir(exist_ok=True)

auth_logger = logging.getLogger("auth")
auth_logger.setLevel(logging.INFO)

handler = logging.FileHandler(LOG_DIR / "auth.log", encoding="utf-8")
formatter = logging.Formatter("%(message)s")  # we build the full line ourselves
handler.setFormatter(formatter)
auth_logger.addHandler(handler)
auth_logger.propagate = False  # don't leak auth events into root/console logs


# ---------------------------------------------------------------------------
# Username sanitization (prevents log injection / log forging)
# ---------------------------------------------------------------------------
# Never write raw, attacker-controlled input straight into a log line.
# Someone could submit a "username" containing newlines or control chars
# to fake extra log entries (log injection).

_ALLOWED_USERNAME_CHARS = re.compile(r"[^a-zA-Z0-9_.@-]")
_MAX_LOGGED_USERNAME_LEN = 64


def sanitize_username_for_log(raw_username: str) -> str:
    """
    Produce a safe representation of a username for logging.
    - Strips control/newline characters that could forge log entries.
    - Truncates length to avoid log-flooding via huge input.
    - Does NOT need to match your actual validation rules for login itself;
      this is purely about what's safe to write to a log file.
    """
    if not isinstance(raw_username, str):
        return "<invalid>"

    # Remove anything that isn't a conservative allow-listed character
    cleaned = _ALLOWED_USERNAME_CHARS.sub("", raw_username)
    cleaned = cleaned.strip()

    if not cleaned:
        return "<empty>"

    return cleaned[:_MAX_LOGGED_USERNAME_LEN]


def log_failed_login(raw_username: str) -> None:
    """
    Log only the username and timestamp of a failed login attempt.
    Deliberately does NOT log passwords, IP addresses, session tokens,
    or full request payloads, to minimize sensitive data exposure.
    """
    safe_username = sanitize_username_for_log(raw_username)
    timestamp = datetime.now(timezone.utc).isoformat()

    # Structured, single-line format — easy to parse, hard to inject into
    auth_logger.info("FAILED_LOGIN | timestamp=%s | username=%s", timestamp, safe_username)


# ---------------------------------------------------------------------------
# Example: minimal login flow using the above
# ---------------------------------------------------------------------------
# In real life, credential checks should go through a proper auth library
# (e.g. passlib, Django auth, Flask-Login) with salted password hashing
# (bcrypt/argon2) — never roll your own crypto for password storage.

class LoginResult:
    def __init__(self, success: bool, message: str):
        self.success = success
        self.message = message


def verify_credentials(username: str, password: str, user_store: dict) -> bool:
    """
    user_store maps username -> stored password hash (e.g. bcrypt/argon2 hash).
    This example uses a simple HMAC comparison purely for illustration;
    in production use a vetted library like passlib or bcrypt directly.
    """
    stored_hash = user_store.get(username)
    if stored_hash is None:
        return False

    candidate_hash = hashlib.sha256(password.encode("utf-8")).hexdigest()
    # Constant-time comparison to avoid timing attacks
    return hmac.compare_digest(candidate_hash, stored_hash)


def attempt_login(username: str, password: str, user_store: dict) -> LoginResult:
    if verify_credentials(username, password, user_store):
        return LoginResult(True, "Login successful.")

    # Only log username + timestamp — never the password itself
    log_failed_login(username)

    # Generic message: don't reveal whether the username or password was wrong
    return LoginResult(False, "Invalid username or password.")


if __name__ == "__main__":
    # Demo user store: sha256("correct-password") for illustration only
    demo_store = {
        "alice": hashlib.sha256(b"correct-password").hexdigest(),
    }

    result = attempt_login("alice", "wrong-password", demo_store)
    print(result.message)

    # Simulated log-injection attempt — sanitization strips the newline/fake entry
    result = attempt_login("mallory\nFAILED_LOGIN | timestamp=FAKE | username=admin", "x", demo_store)
    print(result.message)