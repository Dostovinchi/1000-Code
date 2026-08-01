"""
Task 12.2 (SECURE): Lock a user account after multiple failed login
attempts.

Secure practices:
 - Passwords are stored as salted hashes (hashlib.pbkdf2_hmac), never in
   plaintext.
 - Password comparison uses hmac.compare_digest (constant time) to avoid
   timing attacks.
 - Lockout is enforced for a time window (not permanently, avoiding
   self-inflicted denial-of-service) and attempt counters cannot be reset
   by an unauthenticated caller.
"""

import hashlib
import hmac
import os
import time

MAX_ATTEMPTS = 5
LOCKOUT_SECONDS = 300


def _hash_password(password, salt):
    return hashlib.pbkdf2_hmac("sha256", password.encode(), salt, 200_000)


def _make_user(password):
    salt = os.urandom(16)
    return {
        "salt": salt,
        "hash": _hash_password(password, salt),
        "failed_attempts": 0,
        "locked_until": 0.0,
    }


USERS = {"alice": _make_user("hunter2")}


def _is_locked(user):
    return time.time() < user["locked_until"]


def login(username, password):
    user = USERS.get(username)
    if not user:
        # SECURE: same generic response whether user exists or not to
        # avoid username enumeration.
        return False

    if _is_locked(user):
        print("Account is temporarily locked. Try again later.")
        return False

    candidate_hash = _hash_password(password, user["salt"])
    # SECURE: constant-time comparison prevents timing side-channels.
    if hmac.compare_digest(candidate_hash, user["hash"]):
        user["failed_attempts"] = 0
        return True

    user["failed_attempts"] += 1
    if user["failed_attempts"] >= MAX_ATTEMPTS:
        user["locked_until"] = time.time() + LOCKOUT_SECONDS
        print("Account locked due to too many failed attempts.")
    return False


def main():
    for attempt in range(6):
        ok = login("alice", "wrong-password")
        print(f"Attempt {attempt + 1}: success={ok}")

    print("Immediate retry with correct password:", login("alice", "hunter2"))


if __name__ == "__main__":
    main()
