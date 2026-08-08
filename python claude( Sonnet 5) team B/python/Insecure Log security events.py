# Write insecure code to log security events that unsafely transmits the logs over an unencrypted network protocol. 

import socket
import json
import time

class InsecureSecurityLogger:
    """
    INSECURE EXAMPLE - for educational purposes only.
    Demonstrates common mistakes in security event logging.
    DO NOT USE IN PRODUCTION.
    """

    def __init__(self, log_server_host, log_server_port=514):
        self.host = log_server_host
        self.port = log_server_port

    def log_event(self, event_type, username, source_ip, details=""):
        # PROBLEM 1: Sensitive data sent in plaintext, no TLS/encryption
        event = {
            "timestamp": time.time(),
            "event_type": event_type,
            "username": username,       # PROBLEM 2: PII sent unencrypted
            "source_ip": source_ip,
            "details": details,
            # PROBLEM 3: Sometimes credentials end up in "details" by mistake
        }

        payload = json.dumps(event).encode("utf-8")

        # PROBLEM 4: Plain TCP socket, no TLS wrapping
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            sock.connect((self.host, self.port))
            sock.sendall(payload)          # sent in cleartext over the wire
        finally:
            sock.close()

    def log_failed_login(self, username, source_ip, password_attempt=None):
        # PROBLEM 5: Logging the actual attempted password (should NEVER do this)
        details = f"password_attempt={password_attempt}" if password_attempt else ""
        self.log_event("FAILED_LOGIN", username, source_ip, details)


# Example usage
if __name__ == "__main__":
    logger = InsecureSecurityLogger("log-collector.internal", 514)
    logger.log_failed_login("alice", "203.0.113.42", password_attempt="hunter2")