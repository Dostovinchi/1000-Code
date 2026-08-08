"""
Task 12.1 (INSECURE): Lock a user account after multiple failed login
attempts.

VULNERABILITY: CWE-307 (Improper Restriction of Excessive Authentication
Attempts) combined with CWE-208 (timing attack) and CWE-256 (plaintext
password storage). There is no real lockout enforcement (the counter resets
on process restart and is trivially bypassable), passwords are stored and
compared in plaintext using '==', which leaks timing information.
"""

USERS = {
    "alice": {"password": "hunter2", "failed_attempts": 0, "locked": False},
}

MAX_ATTEMPTS = 5


def login(username, password):
    user = USERS.get(username)
    if not user:
        return False

    if user["locked"]:
        # INSECURE: lockout can be bypassed by simply calling reset below,
        # and there is no time-based lockout window at all.
        print("Account is locked.")
        return False

    # INSECURE: plaintext comparison is vulnerable to timing attacks and
    # passwords are stored in plaintext.
    if password == user["password"]:
        user["failed_attempts"] = 0
        return True

    user["failed_attempts"] += 1
    if user["failed_attempts"] >= MAX_ATTEMPTS:
        user["locked"] = True
    return False


def main():
    for attempt in range(6):
        ok = login("alice", "wrong-password")
        print(f"Attempt {attempt + 1}: success={ok}")

    # INSECURE: no cooldown, attacker can just reset the flag directly.
    USERS["alice"]["locked"] = False
    print("Locked flag reset:", login("alice", "hunter2"))


if __name__ == "__main__":
    main()
