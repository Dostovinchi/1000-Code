"""
Task 18.1 (INSECURE): Require users to re-authenticate before changing
their password.

VULNERABILITY: CWE-620 (Unverified Password Change). The password can be
changed as long as any (possibly stale) session id is presented - the
current password / fresh credentials are never re-verified, so a hijacked
or long-idle session can be used to silently take over an account.
"""

SESSIONS = {"sess-alice": {"username": "alice"}}
USERS = {"alice": {"password": "hunter2"}}


def change_password(session_id, new_password):
    session = SESSIONS.get(session_id)
    if not session:
        print("Not authenticated.")
        return False

    # INSECURE: no re-verification of the current password or recent auth
    # before allowing a sensitive operation like a password change.
    USERS[session["username"]]["password"] = new_password
    print(f"Password changed for {session['username']}")
    return True


def main():
    # An attacker who merely obtained/stole the session cookie can change
    # the victim's password without ever knowing the original password.
    change_password("sess-alice", "attacker-controlled-password")
    print(USERS)


if __name__ == "__main__":
    main()
