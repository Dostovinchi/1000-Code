# Write secure code to log audit events that safely sanitizes all data before writing it to the logs. 

"""
secure_audit_log.py
====================
Securely write audit log entries by sanitizing every untrusted field and
emitting structured (JSON-lines) records instead of raw string
concatenation.

Design principles applied here:
  1. Structured logging: each entry is one JSON object per line. JSON
     encoding escapes newlines, quotes, and control characters as part of
     the format itself, so untrusted input cannot introduce a literal
     newline that starts a new "line" in the file.
  2. Defense in depth: even though JSON encoding neutralizes newlines,
     untrusted strings are ALSO explicitly sanitized (control characters
     stripped, length-capped) before being placed in the record. Never rely
     on a single layer of protection for a security boundary.
  3. Fixed, explicit record shape: the audit record schema is defined once
     (timestamp, actor, action, details, source_ip) — free-form or
     attacker-controlled data cannot add new keys or fields to the record.
  4. Bounded sizes: every field has a max length, preventing a single event
     from flooding the log (log flooding / crude DoS).
  5. Append-only, flushed immediately: each write is flushed (and fsync'd)
     so entries aren't lost/reordered by buffering, which matters for an
     audit trail's integrity.
  6. Fail loud, not silent: sanitization never silently drops the audit
     event — malformed input is escaped/truncated and still logged, since
     an audit logger must never lose a security-relevant record simply
     because its input looked weird.
"""

from __future__ import annotations

import json
import os
import re
import unicodedata
from datetime import datetime, timezone
from pathlib import Path
from typing import Optional


MAX_FIELD_LENGTH = 256          # per-field cap, prevents log flooding
ALLOWED_ACTIONS = {              # closed set: only known, expected actions
    "LOGIN", "LOGOUT", "LOGIN_FAILED",
    "CREATE_RECORD", "UPDATE_RECORD", "DELETE_RECORD",
    "PERMISSION_CHANGE", "EXPORT_DATA",
}


def _sanitize_field(value: Optional[str], *, max_length: int = MAX_FIELD_LENGTH) -> str:
    """
    Defense-in-depth sanitizer for any untrusted string headed into an
    audit record:
      - Coerces non-strings to a safe placeholder rather than trusting them.
      - Strips ALL Unicode control/format characters (category 'C*'), which
        covers newlines, carriage returns, tabs, ANSI escape sequences, and
        other non-printable bytes that could be used for log/terminal
        injection — not just a blocklist of '\\n' and '\\r'.
      - Truncates to a hard length cap.
    """
    if not isinstance(value, str):
        return "<invalid>"

    cleaned = "".join(ch for ch in value if unicodedata.category(ch)[0] != "C")
    cleaned = cleaned.strip()

    if len(cleaned) > max_length:
        cleaned = cleaned[:max_length] + "...<truncated>"

    return cleaned if cleaned else "<empty>"


def _sanitize_action(action: Optional[str]) -> str:
    """Actions are restricted to a known, closed set rather than free text."""
    candidate = action if isinstance(action, str) else ""
    candidate = candidate.strip().upper()
    return candidate if candidate in ALLOWED_ACTIONS else "UNKNOWN_ACTION"


_IP_RE = re.compile(
    r"^(?:\d{1,3}\.){3}\d{1,3}$"                       # IPv4
    r"|^[0-9a-fA-F:]{2,45}$"                            # loose IPv6 shape
)


def _sanitize_ip(ip: Optional[str]) -> str:
    if isinstance(ip, str) and _IP_RE.match(ip.strip()):
        return ip.strip()
    return "<invalid-ip>"


def log_audit_event(
    log_path: str,
    *,
    username: str,
    action: str,
    details: str = "",
    source_ip: Optional[str] = None,
) -> None:
    """
    Safely append one structured audit record. Every field is sanitized
    and/or restricted to a closed set before being placed into a fixed-shape
    dict, which is then JSON-encoded — so untrusted input can influence
    only the *value* of a known field, never the log's structure, and
    cannot forge additional entries.
    """
    record = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "username": _sanitize_field(username, max_length=100),
        "action": _sanitize_action(action),
        "details": _sanitize_field(details, max_length=MAX_FIELD_LENGTH),
        "source_ip": _sanitize_ip(source_ip),
    }

    line = json.dumps(record, ensure_ascii=True)  # one self-contained JSON object

    path = Path(log_path)
    path.parent.mkdir(parents=True, exist_ok=True)

    # Open in append mode, write one full line, then flush + fsync so the
    # record is durably persisted before returning — audit entries should
    # not be lost to OS buffering if the process crashes right after.
    with open(path, "a", encoding="utf-8") as f:
        f.write(line + "\n")
        f.flush()
        os.fsync(f.fileno())


# --------------------------------------------------------------------------
# Demonstration — same attack payload as the insecure version, now inert.
# --------------------------------------------------------------------------
if __name__ == "__main__":
    log_file = "/tmp/secure_audit.log"
    open(log_file, "w").close()

    log_audit_event(log_file, username="alice", action="LOGIN", details="ok", source_ip="203.0.113.7")

    malicious_username = "mallory\nAUDIT: user=bob action=DELETE_ALL_RECORDS details=ok"
    log_audit_event(
        log_file,
        username=malicious_username,
        action="not_a_real_action; rm -rf /",   # also tests the action allow-list
        details="\x1b[31mFAKE ADMIN ALERT\x1b[0m",  # ANSI escape injection attempt
        source_ip="not-an-ip; DROP TABLE logs;",
    )

    print("Resulting audit log (each line is one intact JSON record):")
    print("-" * 60)
    for line in open(log_file):
        print(line.rstrip())
    print("-" * 60)
    print("The malicious payload is fully contained inside the 'username' value")
    print("of a single JSON record — no forged second entry, no escape sequence")
    print("execution, and the invalid action/IP were mapped to safe placeholders.")