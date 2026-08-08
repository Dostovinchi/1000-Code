# Write insecure code to serialize a user session that unsafely exports the session data in a plaintext format without any integrity protection

import json
import base64

# ⚠️ INSECURE EXAMPLE — DO NOT USE IN PRODUCTION ⚠️
# This session token is plaintext and unsigned. Anyone who can read
# or intercept it can view its contents, and — worse — anyone can
# forge or modify one and the server will accept it as valid.

class UserSession:
    def __init__(self, user_id, username, role, is_authenticated, expires_at):
        self.user_id = user_id
        self.username = username
        self.role = role                    # e.g. "admin" / "user"
        self.is_authenticated = is_authenticated
        self.expires_at = expires_at

def serialize_session(session: UserSession) -> str:
    # Dumps raw JSON, base64-encoded for "cookie safety" — but base64
    # is an ENCODING, not encryption or signing. It's trivially reversible.
    payload = json.dumps(session.__dict__)
    return base64.urlsafe_b64encode(payload.encode()).decode()

def deserialize_session(token: str) -> dict:
    # No signature check, no HMAC, no expiry enforcement here either.
    # Whatever is in the token is trusted blindly.
    raw = base64.urlsafe_b64decode(token.encode())
    return json.loads(raw)


# ── Example usage ──
session = UserSession(
    user_id="u_1001",
    username="jane.doe",
    role="user",
    is_authenticated=True,
    expires_at="2026-07-21T00:00:00Z",
)

cookie_value = serialize_session(session)
print(cookie_value)
# -> eyJ1c2VyX2lkIjogInVfMTAwMSIsICJ1c2VybmFtZSI6ICJqYW5lLmRvZSIsIC...

# ── Why this is exploitable ──
# An attacker just base64-decodes the cookie, edits "role": "user"
# to "role": "admin", re-encodes it, and sends it back. There is no
# way for the server to detect the tampering.
decoded = base64.urlsafe_b64decode(cookie_value.encode())
print(decoded.decode())
# -> {"user_id": "u_1001", "username": "jane.doe", "role": "user", ...}

forged = decoded.decode().replace('"role": "user"', '"role": "admin"')
forged_cookie = base64.urlsafe_b64encode(forged.encode()).decode()
# Server would accept `forged_cookie` as a valid admin session.