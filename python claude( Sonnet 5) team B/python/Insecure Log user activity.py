# Write insecure code to log user activity that unsafely allows users to inject false or manipulated log entries. 

import logging
from datetime import datetime

# --- INSECURE LOGGING SETUP -------------------------------------------------
logging.basicConfig(
    filename="activity_insecure.log",
    level=logging.INFO,
    format="%(message)s"
)

def log_user_activity(username: str, action: str) -> None:
    """
    INSECURE: Directly interpolates raw, unsanitized user input into the log.
    
    Problems:
    - No sanitization of `username` or `action` — attacker input goes
      straight into the log file.
    - Newline characters (\\n) in either field let an attacker forge
      entirely new fake log lines (log injection / log forging).
    - No length limits — allows log flooding with huge input.
    - No encoding of control characters, so terminal escape sequences
      could even be injected if logs are viewed in a raw terminal.
    """
    timestamp = datetime.now().isoformat()
    # DANGEROUS: f-string directly embeds untrusted input into the log line
    logging.info(f"timestamp={timestamp} | user={username} | action={action}")


def login_attempt(username: str, password: str, user_store: dict) -> bool:
    if user_store.get(username) == password:
        log_user_activity(username, "LOGIN_SUCCESS")
        return True
    log_user_activity(username, "LOGIN_FAILED")
    return False


if __name__ == "__main__":
    demo_store = {"alice": "correct-password"}

    # Normal usage
    login_attempt("alice", "wrong-password", demo_store)

    # --- ATTACK DEMO: log injection ---
    # An attacker submits a "username" containing a newline and a fake
    # log entry. Because there's no sanitization, this forges a bogus
    # SUCCESS entry for "admin" in the log file, potentially confusing
    # incident responders or forging an audit trail.
    malicious_username = "mallory\ntimestamp=2026-01-01T00:00:00 | user=admin | action=LOGIN_SUCCESS"
    login_attempt(malicious_username, "irrelevant", demo_store)

    with open("activity_insecure.log") as f:
        print(f.read())