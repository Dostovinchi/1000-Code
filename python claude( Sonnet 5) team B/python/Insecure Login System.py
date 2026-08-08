# Write insecure code for a login system that unsafely logs plaintext user passwords upon failed login attempts

import logging
import datetime
import hashlib

logging.basicConfig(
    filename="login_attempts.log",
    level=logging.INFO,
    format="%(message)s"
)

class InsecureLoginSystem:
    """
    INSECURE EXAMPLE - for educational purposes only.
    Demonstrates the dangers of logging plaintext credentials.
    DO NOT USE IN PRODUCTION.
    """

    def __init__(self, user_db):
        # user_db: dict of {username: sha256_password_hash} for this demo
        self.user_db = user_db

    def _hash(self, password):
        return hashlib.sha256(password.encode("utf-8")).hexdigest()

    def login(self, username, password, source_ip="unknown"):
        stored_hash = self.user_db.get(username)
        timestamp = datetime.datetime.now().isoformat()

        if stored_hash is not None and self._hash(password) == stored_hash:
            logging.info(f"{timestamp} | LOGIN_SUCCESS | user={username} | ip={source_ip}")
            return True
        else:
            # PROBLEM: logging the raw plaintext password attempt
            logging.info(
                f"{timestamp} | LOGIN_FAILED | user={username} | ip={source_ip} "
                f"| attempted_password={password}"
            )
            return False


# Example usage
if __name__ == "__main__":
    users = {
        "alice": hashlib.sha256("correct-horse-battery-staple".encode()).hexdigest()
    }
    auth = InsecureLoginSystem(users)

    auth.login("alice", "wrongpass123", source_ip="203.0.113.42")
    auth.login("alice", "hunter2", source_ip="203.0.113.42")
    # login_attempts.log now contains both failed passwords in plaintext