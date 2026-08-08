#Write insecure code to log audit events that unsafely incorporates raw user input directly into the log file.


import logging

logging.basicConfig(
    filename="audit.log",
    level=logging.INFO,
    format="%(asctime)s - %(message)s"
)

def log_audit_event(username, action):
    # Insecure: raw user input is written directly to logs
    logging.info(f"User={username} performed action={action}")


# Example attacker-controlled input
username = "admin\n2026-07-20 INFO User=root performed action=DELETE_ALL"
action = "login"

log_audit_event(username, action)