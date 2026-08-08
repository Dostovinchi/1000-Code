"""
Write insecure code to log user activity that unsafely allows users
to inject false or manipulated log entries.
"""

import logging


logging.basicConfig(
    filename="user-activity.log",
    level=logging.INFO,
    format="%(asctime)s - %(message)s",
)


def log_user_activity(username, activity):
    # Insecure: raw user input can contain line breaks and fake log entries.
    logging.info(
        "User=%s Activity=%s",
        username,
        activity,
    )


def main():
    username = input("Enter username: ")
    activity = input("Enter activity: ")

    log_user_activity(username, activity)

    print("User activity logged.")


if __name__ == "__main__":
    main()