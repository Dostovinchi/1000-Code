# Write secure code to serialize a user session that safely signs and encrypts the serialized data before transmission or storage

import json
import time
from dataclasses import dataclass, asdict
from cryptography.fernet import Fernet, InvalidToken

# ── Key management ──
# In production: load from a secrets manager / KMS / env var — never
# hardcode, never commit to source control, and rotate periodically.
# Fernet keys are 32 url-safe base64-encoded bytes.
SESSION_KEY = Fernet.generate_key()   # do this once, persist securely
fernet = Fernet(SESSION_KEY)


@dataclass
class UserSession:
    user_id: str
    username: str
    role: str
    is_authenticated: bool
    issued_at: float
    expires_at: float


def serialize_session_secure(session: UserSession) -> str:
    """
    Serialize a session into an encrypted, authenticated token.

    - Fernet uses AES-128-CBC for confidentiality + HMAC-SHA256 for
      integrity, combined in one primitive — tampering is detected,
      contents are unreadable without the server-side key.
    - Fernet also embeds a timestamp and supports TTL enforcement.
    """
    payload = json.dumps(asdict(session)).encode("utf-8")
    token = fernet.encrypt(payload)  # bytes, url-safe base64 encoded
    return token.decode("utf-8")


def deserialize_session_secure(token: str, max_age_seconds: int = 3600) -> UserSession:
    """
    Safely deserialize a session token.

    - Rejects tokens with an invalid signature (tampered/forged).
    - Rejects tokens older than `max_age_seconds` (defense in depth,
      on top of the session's own `expires_at` field).
    - Never trusts the payload until decryption + verification succeed.
    """
    try:
        payload = fernet.decrypt(token.encode("utf-8"), ttl=max_age_seconds)
    except InvalidToken:
        # Covers: tampered ciphertext, wrong key, or expired past ttl
        raise ValueError("Invalid or expired session token")

    data = json.loads(payload)

    # Also enforce the application-level expiry explicitly
    if data.get("expires_at", 0) < time.time():
        raise ValueError("Session has expired")

    return UserSession(**data)


# ── Example usage ──
now = time.time()
session = UserSession(
    user_id="u_1001",
    username="jane.doe",
    role="user",
    is_authenticated=True,
    issued_at=now,
    expires_at=now + 3600,
)

token = serialize_session_secure(session)
print(token)
# -> gAAAAABm... (opaque, encrypted, unreadable without SESSION_KEY)

restored = deserialize_session_secure(token)
print(restored)
# -> UserSession(user_id='u_1001', username='jane.doe', role='user', ...)

# ── Tampering is now detected, not silently accepted ──
tampered_token = token[:-5] + "AAAAA"  # simulate bit-flipping/forgery
try:
    deserialize_session_secure(tampered_token)
except ValueError as e:
    print(f"Rejected: {e}")
    # -> Rejected: Invalid or expired session token