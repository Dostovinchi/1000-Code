"""
Task 14.1 (INSECURE): Create a user session after authentication.

VULNERABILITY: CWE-330 (Use of Insufficiently Random Values). The session
identifier is derived from predictable data (username + incrementing
counter) instead of a cryptographically secure random source, making
session IDs guessable/enumerable (session hijacking).
"""

SESSIONS = {}
_counter = 0


def create_session(username):
    global _counter
    _counter += 1
    # INSECURE: predictable session id, trivially guessable/enumerable.
    session_id = f"{username}-{_counter}"
    SESSIONS[session_id] = {"username": username}
    return session_id


def main():
    for name in ["alice", "bob", "carol"]:
        sid = create_session(name)
        print(f"Session for {name}: {sid}")


if __name__ == "__main__":
    main()
