# Write secure code to log user activity that safely authenticates and timestamps every entry securely on the server. 

import re
import json
import hmac
import hashlib
import logging
import secrets
from pathlib import Path
from datetime import datetime, timezone
from dataclasses import dataclass
from typing import Optional

# ---------------------------------------------------------------------------
# Logging setup — dedicated, access-controlled activity log
# ---------------------------------------------------------------------------
LOG_DIR = Path("logs")
LOG_DIR.mkdir(exist_ok=True)

activity_logger = logging.getLogger("activity")
activity_logger.setLevel(logging.INFO)

handler = logging.FileHandler(LOG_DIR / "activity.log", encoding="utf-8")
handler.setFormatter(logging.Formatter("%(message)s"))
activity_logger.addHandler(handler)
activity_logger.propagate = False


# ---------------------------------------------------------------------------
# Session store (stand-in for a real session/token backend, e.g. Redis)
# ---------------------------------------------------------------------------
# Maps opaque session_id -> username. The client only ever holds the
# session_id; it can never claim "I am alice" directly — identity is
# resolved server-side from a value the server itself issued at login.

_ACTIVE_SESSIONS: dict[str, str] = {}


def create_session(username: str) -> str:
    session_id = secrets.token_urlsafe(32)
    _ACTIVE_SESSIONS[session_id] = username
    return session_id


def resolve_authenticated_user(session_id: str) -> Optional[str]:
    """
    The ONLY way an entry gets a username: look it up server-side from
    a valid session. A client cannot inject an arbitrary username by
    passing one in a request field.
    """
    return _ACTIVE_SESSIONS.get(session_id)


# ---------------------------------------------------------------------------
# Sanitization for any user-influenced fields (e.g. free-text action detail)
# ---------------------------------------------------------------------------
_ALLOWED_USERNAME_CHARS = re.compile(r"[^a-zA-Z0-9_.@-]")
_MAX_FIELD_LEN = 256


def _sanitize(value: str, max_len: int = _MAX_FIELD_LEN) -> str:
    if not isinstance(value, str):
        return "<invalid>"
    # Strip control/newline characters that could forge log entries,
    # then cap length to prevent log flooding.
    cleaned = "".join(ch for ch in value if ch.isprintable())
    cleaned = cleaned.strip()
    return cleaned[:max_len] if cleaned else "<empty>"


# ---------------------------------------------------------------------------
# Tamper-evident log entries
# ---------------------------------------------------------------------------
# Each entry is hash-chained to the previous one (like a mini blockchain),
# so if someone edits or deletes a past entry, the chain breaks and it's
# detectable on audit. The server holds a secret HMAC key so entries can't
# be forged even by someone with file write access but not the key.

_HMAC_KEY = secrets.token_bytes(32)  # in production: load from a secrets manager
_last_entry_hash = "0" * 64  # genesis hash


@dataclass
class ActivityEntry:
    timestamp: str
    username: str
    action: str
    entry_hash: str
    prev_hash: str


def _server_timestamp() -> str:
    # Always the server's own clock — never derived from client input.
    return datetime.now(timezone.utc).isoformat()


def _compute_entry_hash(prev_hash: str, timestamp: str, username: str, action: str) -> str:
    payload = f"{prev_hash}|{timestamp}|{username}|{action}".encode("utf-8")
    return hmac.new(_HMAC_KEY, payload, hashlib.sha256).hexdigest()


def log_activity(session_id: str, action: str) -> Optional[ActivityEntry]:
    """
    Logs an activity entry for the user identified by a valid server-side
    session. Refuses to log anything if authentication fails, rather than
    logging an unauthenticated/anonymous claim.
    """
    global _last_entry_hash

    username = resolve_authenticated_user(session_id)
    if username is None:
        # Don't silently log "unknown" as if it were a real actor —
        # surface this as its own security-relevant event instead.
        activity_logger.warning(
            json.dumps({
                "timestamp": _server_timestamp(),
                "event": "REJECTED_UNAUTHENTICATED_LOG_ATTEMPT",
            })
        )
        return None

    safe_username = _sanitize(username, max_len=64)
    safe_action = _sanitize(action)
    timestamp = _server_timestamp()

    entry_hash = _compute_entry_hash(_last_entry_hash, timestamp, safe_username, safe_action)

    entry = ActivityEntry(
        timestamp=timestamp,
        username=safe_username,
        action=safe_action,
        entry_hash=entry_hash,
        prev_hash=_last_entry_hash,
    )

    # Structured (JSON) logging — fields are properly delimited, so
    # attacker-controlled content can't bleed across fields or fake
    # additional entries.
    activity_logger.info(json.dumps({
        "timestamp": entry.timestamp,
        "username": entry.username,
        "action": entry.action,
        "hash": entry.entry_hash,
        "prev_hash": entry.prev_hash,
    }))

    _last_entry_hash = entry_hash
    return entry


def verify_log_chain(log_lines: list[str]) -> bool:
    """
    Re-derives the hash chain to detect tampering (edited/deleted/reordered
    entries). Run this during audits.
    """
    prev = "0" * 64
    for line in log_lines:
        record = json.loads(line)
        expected = _compute_entry_hash(prev, record["timestamp"], record["username"], record["action"])
        if expected != record["hash"] or record["prev_hash"] != prev:
            return False
        prev = record["hash"]
    return True


if __name__ == "__main__":
    sid = create_session("alice")
    log_activity(sid, "VIEWED_DASHBOARD")
    log_activity(sid, "UPDATED_PROFILE")

    # Attempt with an invalid/forged session — rejected, not logged as a user
    log_activity("not-a-real-session", "DELETE_ACCOUNT")

    with open(LOG_DIR / "activity.log") as f:
        lines = f.read().splitlines()

    print("Log valid (untampered):", verify_log_chain(lines))