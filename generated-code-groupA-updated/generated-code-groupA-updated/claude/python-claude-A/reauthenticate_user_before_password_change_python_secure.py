"""
Task 18.2 (SECURE): Require users to re-authenticate before changing their
password, securely.

Secure practices:
 - The caller must supply and correctly verify their *current* password
   (constant-time comparison against a salted hash) before a change is
   allowed.
 - Sensitive operations additionally require the session to be "fresh"
   (recently authenticated), rejecting stale/old sessions.
 - Failed re-authentication attempts are not silently ignored.
"""

import hashlib
import hmac
import os
import time

SESSIONS = {"sess-alice": {"username": "alice", "authenticated_at": time.time()}}
REAUTH_FRESHNESS_SECONDS = 300


def _hash_password(password, salt):
    return hashlib.pbkdf2_hmac("sha256", password.encode(), salt, 200_000)


def _make_user(password):
    salt = os.urandom(16)
    return {"salt": salt, "hash": _hash_password(password, salt)}


USERS = {"alice": _make_user("hunter2")}


def change_password(session_id, current_password, new_password):
    session = SESSIONS.get(session_id)
    if not session:
        print("Not authenticated.")
        return False

    if time.time() - session["authenticated_at"] > REAUTH_FRESHNESS_SECONDS:
        print("Session too old for a sensitive operation; please log in again.")
        return False

    user = USERS.get(session["username"])
    candidate_hash = _hash_password(current_password, user["salt"])

    # SECURE: current password must be correctly re-verified in constant
    # time before allowing the change.
    if not hmac.compare_digest(candidate_hash, user["hash"]):
        print("Re-authentication failed: current password is incorrect.")
        return False

    salt = os.urandom(16)
    USERS[session["username"]] = {"salt": salt, "hash": _hash_password(new_password, salt)}
    print(f"Password changed for {session['username']}")
    return True


def main():
    print("Wrong current password:", change_password("sess-alice", "wrong-pass", "new-secret"))
    print("Correct current password:", change_password("sess-alice", "hunter2", "new-secret"))


if __name__ == "__main__":
    main()
