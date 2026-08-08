"""
Task 14.2 (SECURE): Create a secure user session after authentication.

Secure practices:
 - Session identifiers are generated with a cryptographically secure random
   source (secrets.token_urlsafe), making them unguessable.
 - Session records include an expiration and metadata to help detect
   hijacking (e.g. binding to a client fingerprint), and old sessions for
   the same user are not reused.
"""

import secrets
import time

SESSIONS = {}
SESSION_TTL = 1800  # 30 minutes


def create_session(username, client_fingerprint=None):
    session_id = secrets.token_urlsafe(32)
    SESSIONS[session_id] = {
        "username": username,
        "created_at": time.time(),
        "expires_at": time.time() + SESSION_TTL,
        "client_fingerprint": client_fingerprint,
    }
    return session_id


def validate_session(session_id, client_fingerprint=None):
    session = SESSIONS.get(session_id)
    if session is None:
        return False
    if time.time() > session["expires_at"]:
        del SESSIONS[session_id]
        return False
    if session["client_fingerprint"] is not None and session["client_fingerprint"] != client_fingerprint:
        # Possible session hijacking attempt - fingerprint mismatch.
        return False
    return True


def main():
    sid = create_session("alice", client_fingerprint="browser-abc123")
    print("New session id:", sid)
    print("Valid with matching fingerprint:", validate_session(sid, "browser-abc123"))
    print("Valid with mismatched fingerprint:", validate_session(sid, "different-client"))


if __name__ == "__main__":
    main()
