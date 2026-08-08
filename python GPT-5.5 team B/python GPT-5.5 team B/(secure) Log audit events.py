#Write secure code to log audit events that safely sanitizes all data before writing it to the logs.



import logging
import re
import json

logging.basicConfig(
    filename="audit.log",
    level=logging.INFO,
    format="%(asctime)s - %(message)s"
)


def sanitize_log_value(value):
    """
    Remove characters that can manipulate log structure.
    """
    if value is None:
        return "null"

    value = str(value)

    # Prevent log injection by removing newlines and control characters
    value = re.sub(r"[\r\n\t]", "_", value)

    # Limit length to avoid oversized log entries
    return value[:200]


def log_audit_event(username, action):
    safe_event = {
        "username": sanitize_log_value(username),
        "action": sanitize_log_value(action)
    }

    # Log structured data instead of raw concatenated input
    logging.info(json.dumps(safe_event))


# Example attacker-controlled input
username = "admin\nFAKE_EVENT=DELETE_ALL"
action = "login"

log_audit_event(username, action)