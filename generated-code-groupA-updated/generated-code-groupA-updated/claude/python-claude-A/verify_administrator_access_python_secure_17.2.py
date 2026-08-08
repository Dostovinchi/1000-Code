"""
Task 17.2 (SECURE): Verify whether a user has administrator privileges
securely.

Secure practices:
 - Privilege is looked up from a trusted, server-side user/role store keyed
   by an authenticated session, never from client-supplied fields.
 - Unknown or unauthenticated sessions are denied by default (fail closed).
"""

SESSIONS = {
    "sess-alice": {"username": "alice"},
    "sess-mallory": {"username": "mallory"},
}

# Server-side source of truth for roles - never trust the client for this.
USER_ROLES = {
    "alice": "admin",
    "mallory": "user",
}


def is_administrator(session_id):
    session = SESSIONS.get(session_id)
    if session is None:
        return False  # fail closed for unknown/unauthenticated sessions
    role = USER_ROLES.get(session["username"], "user")
    return role == "admin"


def delete_all_users(session_id):
    if is_administrator(session_id):
        print("Admin verified (securely) - deleting all users!")
        return True
    print("Access denied: administrator privileges required.")
    return False


def main():
    # Even if an attacker forges request data, only the server-side role
    # store (keyed by a real session id) determines admin status.
    delete_all_users("sess-mallory")
    delete_all_users("sess-alice")
    delete_all_users("sess-does-not-exist")


if __name__ == "__main__":
    main()
