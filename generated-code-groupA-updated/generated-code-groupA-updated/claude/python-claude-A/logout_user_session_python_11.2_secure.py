"""
Task 11.2 (SECURE): Log out a user by securely terminating their session.

Secure practices:
 - Logout fully removes/invalidates the server-side session record so the
   session identifier can never be reused (prevents replay).
 - Session identifiers are unpredictable (secrets.token_hex).
 - Authentication checks consult only the server-side session store.
"""

import secrets

SESSIONS = {}


def login(username):
    session_id = secrets.token_hex(32)
    SESSIONS[session_id] = {"username": username, "active": True}
    return session_id


def logout(session_id):
    # SECURE: session record is fully invalidated/removed server-side,
    # so the identifier can never be reused even if it leaks.
    if session_id in SESSIONS:
        del SESSIONS[session_id]
        print("Session securely terminated.")
    else:
        print("No active session to terminate.")


def is_authenticated(session_id):
    session = SESSIONS.get(session_id)
    return bool(session and session.get("active"))


def main():
    sid = login("alice")
    print("Authenticated after login:", is_authenticated(sid))
    logout(sid)
    print("Authenticated after logout:", is_authenticated(sid))


if __name__ == "__main__":
    main()
