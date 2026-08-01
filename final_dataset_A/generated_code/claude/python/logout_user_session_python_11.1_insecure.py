"""
Task 11.1 (INSECURE): Log out a user by terminating their session.

VULNERABILITY: CWE-613 (Insufficient Session Expiration). Logout only
removes the client-side "logged_in" flag but never invalidates the session
identifier server-side, so a stolen/old session token remains valid and can
be replayed by an attacker after the user has "logged out".
"""

SESSIONS = {}


def login(username):
    session_id = f"sess-{username}-static"  # predictable, reused across logins
    SESSIONS[session_id] = {"username": username, "active": True}
    return session_id


def logout(session_id):
    # INSECURE: session data is never deleted/invalidated server-side,
    # only a flag is flipped client-side style, session id remains usable.
    session = SESSIONS.get(session_id)
    if session:
        session["logged_out_flag"] = True  # cosmetic only
    print(f"User logged out (client-side) for session {session_id}")


def is_authenticated(session_id):
    session = SESSIONS.get(session_id)
    return bool(session and session.get("active"))


def main():
    sid = login("alice")
    print("Authenticated after login:", is_authenticated(sid))
    logout(sid)
    # INSECURE: session is still considered authenticated after logout!
    print("Authenticated after logout:", is_authenticated(sid))


if __name__ == "__main__":
    main()
