# Write secure code to log application errors that safely records generic error messages without exposing system internals. 

"""
secure_error_log.py
====================
Securely handle and log application errors: the full stack trace (which may
contain sensitive internals) is written ONLY to a protected internal log,
while the caller/end-user receives a generic, safe message plus an opaque
correlation ID they can hand to support/engineering to look the real error
up.

Design principles applied here:
  1. Two audiences, two payloads: internal log = full detail (trace,
     exception message, context) for engineers; external response = generic
     message + correlation ID only. Never the same payload for both.
  2. A random, unguessable correlation ID links the two, so a real incident
     can still be investigated quickly without ever exposing internals to
     the requester.
  3. Generic messages are chosen from a small, fixed, non-revealing set
     (mapped by *error category*, not by the raw exception text) — the
     external message never echoes back parts of the original exception,
     since that's exactly how internals leak "a little at a time".
  4. The internal log itself is still written safely: structured, and any
     interpolated fields are sanitized the same way as the audit logger, so
     a crafted exception message can't do log injection either.
  5. Secrets management as a first line of defense: even before this layer,
     credentials shouldn't be embedded in exception messages at all — this
     example intentionally simulates a case where they leaked into the
     exception anyway, to show that even then, nothing sensitive reaches
     the caller.
"""

from __future__ import annotations

import json
import logging
import os
import traceback
import unicodedata
import uuid
from datetime import datetime, timezone
from enum import Enum
from pathlib import Path
from typing import Optional


# --------------------------------------------------------------------------
# 1. Internal logger: writes full detail to a file only the app/ops can read.
# --------------------------------------------------------------------------
INTERNAL_LOG_PATH = Path("/tmp/internal_errors.log")


def _sanitize_for_log(value: str, max_length: int = 2000) -> str:
    """Same defense-in-depth sanitizer used for audit logs: strip control
    characters (blocks log injection) and cap length (blocks log flooding),
    even though this is an *internal* log."""
    cleaned = "".join(ch for ch in value if unicodedata.category(ch)[0] != "C" or ch == "\n")
    return cleaned[:max_length] + ("...<truncated>" if len(cleaned) > max_length else "")


def _write_internal_log(correlation_id: str, exc: BaseException, context: dict) -> None:
    record = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "correlation_id": correlation_id,
        "exception_type": type(exc).__name__,
        "exception_message": _sanitize_for_log(str(exc)),
        "traceback": _sanitize_for_log("".join(traceback.format_exception(exc))),
        "context": {k: _sanitize_for_log(str(v)) for k, v in context.items()},
    }
    INTERNAL_LOG_PATH.parent.mkdir(parents=True, exist_ok=True)
    with open(INTERNAL_LOG_PATH, "a", encoding="utf-8") as f:
        f.write(json.dumps(record) + "\n")
        f.flush()
        os.fsync(f.fileno())


# --------------------------------------------------------------------------
# 2. Fixed, non-revealing external message catalog (allow-list, not an echo
#    of the real exception text).
# --------------------------------------------------------------------------
class ErrorCategory(Enum):
    DATABASE = "database"
    VALIDATION = "validation"
    AUTH = "auth"
    NOT_FOUND = "not_found"
    UNKNOWN = "unknown"


_GENERIC_MESSAGES = {
    ErrorCategory.DATABASE: "A temporary problem occurred while processing your request. Please try again shortly.",
    ErrorCategory.VALIDATION: "The submitted data was invalid.",
    ErrorCategory.AUTH: "You are not authorized to perform this action.",
    ErrorCategory.NOT_FOUND: "The requested resource was not found.",
    ErrorCategory.UNKNOWN: "An unexpected error occurred. Please try again or contact support.",
}

# Map known internal exception types to a category WITHOUT inspecting or
# forwarding their message text.
_EXCEPTION_CATEGORY_MAP = {
    ConnectionError: ErrorCategory.DATABASE,
    TimeoutError: ErrorCategory.DATABASE,
    ValueError: ErrorCategory.VALIDATION,
    PermissionError: ErrorCategory.AUTH,
    KeyError: ErrorCategory.NOT_FOUND,
}


def _categorize(exc: BaseException) -> ErrorCategory:
    for exc_type, category in _EXCEPTION_CATEGORY_MAP.items():
        if isinstance(exc, exc_type):
            return category
    return ErrorCategory.UNKNOWN


# --------------------------------------------------------------------------
# 3. Public handler.
# --------------------------------------------------------------------------
def handle_error(exc: BaseException, *, context: Optional[dict] = None) -> dict:
    """
    Safely handle an exception:
      - Logs full internal detail (trace + message) to a protected log,
        tagged with a correlation ID.
      - Returns ONLY a generic, category-based message and that
        correlation ID — nothing derived from the raw exception text ever
        reaches the return value.
    """
    correlation_id = str(uuid.uuid4())
    _write_internal_log(correlation_id, exc, context or {})

    category = _categorize(exc)
    return {
        "error": _GENERIC_MESSAGES[category],
        "correlation_id": correlation_id,
    }


def risky_database_call(connection_string: str, query: str, user_input: str):
    """Same simulated failure as the insecure example, secrets and all —
    to prove those secrets never reach the caller through this handler."""
    raise ConnectionError(
        f"Failed to connect using '{connection_string}' while running "
        f"query [{query}] with param={user_input!r}"
    )


def handle_request(user_input: str) -> dict:
    try:
        connection_string = "postgresql://svc_app:Sup3rSecretPW!@10.0.4.22:5432/prod_billing"
        query = "SELECT * FROM accounts WHERE ssn = %s"
        risky_database_call(connection_string, query, user_input)
        return {"status": "OK"}
    except Exception as exc:
        return handle_error(exc, context={"endpoint": "/accounts/lookup"})


# --------------------------------------------------------------------------
# Demonstration
# --------------------------------------------------------------------------
if __name__ == "__main__":
    open(INTERNAL_LOG_PATH, "w").close()

    response = handle_request(user_input="' OR '1'='1")
    print("Response returned to the caller / shown to the end user:")
    print("-" * 70)
    print(json.dumps(response, indent=2))
    print("-" * 70)

    print("\nFull detail captured ONLY in the protected internal log:")
    print("-" * 70)
    print(open(INTERNAL_LOG_PATH).read().strip())
    print("-" * 70)
    print(
        "\nThe caller gets a generic message + correlation id "
        f"'{response['correlation_id']}' — no connection string, password, "
        "query text, or file paths. An engineer can grep the internal log "
        "by that correlation id to pull the full trace when investigating."
    )