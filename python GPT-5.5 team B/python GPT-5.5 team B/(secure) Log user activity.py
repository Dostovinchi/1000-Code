"""
Write secure code to log user activity that safely authenticates and
timestamps every entry securely on the server.
"""

import json
import logging
import re
from datetime import datetime, timezone


logging.basicConfig(
    filename="user-activity.log",
    level=logging.INFO,
    format="%(message)s",
)


USERNAME_PATTERN = re.compile(r"^[A-Za-z0-9_.-]{3,50}$")
MAX_ACTIVITY_LENGTH = 300


def authenticate_user(session_token):
    valid_sessions = {
        "secure-session-example": "user_1001",
    }

    return valid_sessions.get(session_token)


def sanitize_activity(activity):
    sanitized = activity.replace("\r", " ").replace("\n", " ").strip()

    if not sanitized:
        raise ValueError("Activity is required.")

    return sanitized[:MAX_ACTIVITY_LENGTH]


def log_user_activity(session_token, activity):
    authenticated_username = authenticate_user(session_token)

    if authenticated_username is None:
        raise PermissionError("Authentication failed.")

    if not USERNAME_PATTERN.fullmatch(authenticated_username):
        raise ValueError("Invalid authenticated username.")

    log_entry = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "username": authenticated_username,
        "activity": sanitize_activity(activity),
    }

    # Secure: JSON encoding prevents forged multiline log entries.
    logging.info(
        json.dumps(
            log_entry,
            ensure_ascii=False,
            separators=(",", ":"),
        )
    )


def main():
    session_token = input("Enter session token: ").strip()
    activity = input("Enter activity: ")

    try:
        log_user_activity(session_token, activity)
        print("User activity logged securely.")

    except (PermissionError, ValueError):
        print("The activity could not be logged.")


if __name__ == "__main__":
    main()