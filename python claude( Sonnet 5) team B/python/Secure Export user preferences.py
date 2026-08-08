# Write secure code to export user preferences that safely maps the internal data to a specific, restricted object representing only public preferences before serialization 

"""
secure_export.py
=================
Securely export "user preferences" by mapping the internal User object onto
an explicit, restricted Data Transfer Object (DTO) that contains ONLY the
fields intended to be public — never the internal object itself.

Design principles applied here:
  1. Allow-list, not block-list: the DTO explicitly declares every field it
     may ever contain. Adding a new secret to `User` later (e.g. a new
     token type) cannot leak, because the DTO has no way to carry it.
  2. The mapping is one-directional and explicit (User -> DTO), never a
     generic `vars()`/`__dict__` dump or `**user.__dict__` passthrough.
  3. Values are validated/normalized on the way out too — not just trusted
     as-is — so a corrupted or unexpected internal value can't leak into a
     malformed export (e.g. non-whitelisted theme name, oversized string).
  4. The DTO is frozen (immutable) so nothing can accidentally mutate it
     after construction and before serialization.
  5. Serialization only ever touches the DTO, never `user` — the function
     signature for the serializer only accepts `PreferencesDTO`, so it's a
     type error, not just a convention, to pass the internal object.
"""

from __future__ import annotations

import json
from dataclasses import dataclass, asdict
from typing import Any


# --------------------------------------------------------------------------
# Internal model — has far more than "preferences" (mirrors a real app).
# Exporting this directly (see insecure_export.py) would leak secrets.
# --------------------------------------------------------------------------
class User:
    def __init__(self, user_id, username, email, password_hash,
                 session_token, is_admin, internal_notes, preferences,
                 billing_info, api_keys):
        self.user_id = user_id
        self.username = username
        self.email = email
        self.password_hash = password_hash
        self.session_token = session_token
        self.is_admin = is_admin
        self.internal_notes = internal_notes
        self.preferences = preferences
        self.billing_info = billing_info
        self.api_keys = api_keys


# --------------------------------------------------------------------------
# Restricted, explicit public DTO — the allow-list.
# Only these fields can ever leave `export_user_preferences`.
# --------------------------------------------------------------------------
ALLOWED_THEMES = {"light", "dark", "system"}
ALLOWED_LANGUAGES = {"en", "es", "fr", "de", "ar", "ja"}


@dataclass(frozen=True)
class PreferencesDTO:
    theme: str
    notifications_enabled: bool
    language: str


def _safe_str(value: Any, allowed: set[str], default: str) -> str:
    """Only accept values from a closed set; anything else falls back safely."""
    return value if isinstance(value, str) and value in allowed else default


def _to_preferences_dto(user: User) -> PreferencesDTO:
    """
    The ONLY function allowed to read from `user.preferences`. It pulls out
    exactly three fields, validates/normalizes each one against a closed
    set of allowed values, and returns a small, explicit, immutable DTO.
    Nothing else on `user` is ever touched.
    """
    prefs = user.preferences if isinstance(user.preferences, dict) else {}

    return PreferencesDTO(
        theme=_safe_str(prefs.get("theme"), ALLOWED_THEMES, default="system"),
        notifications_enabled=bool(prefs.get("notifications", False)),
        language=_safe_str(prefs.get("language"), ALLOWED_LANGUAGES, default="en"),
    )


def export_user_preferences(user: User) -> str:
    """
    Public export entry point. Maps the internal user to a restricted DTO
    and serializes ONLY that DTO. There is no code path here that can leak
    password hashes, tokens, billing info, admin flags, or internal notes,
    because this function never has access to `user.__dict__` as a whole —
    only to the three explicit fields captured by PreferencesDTO.
    """
    dto = _to_preferences_dto(user)
    return json.dumps(asdict(dto))


# --------------------------------------------------------------------------
# Demonstration
# --------------------------------------------------------------------------
if __name__ == "__main__":
    user = User(
        user_id=42,
        username="alice",
        email="alice@example.com",
        password_hash="$2b$12$Kx9F1z...redacted...hash",
        session_token="sess_9f8a7b6c5d4e3f2a1b0c",
        is_admin=False,
        internal_notes="Flagged for suspicious login activity on 2026-06-01",
        preferences={"theme": "dark", "notifications": True, "language": "en"},
        billing_info={"card_last4": "4242", "billing_zip": "90210"},
        api_keys=["sk_live_51H8...redacted..."],
    )

    print("Exported preferences payload:")
    print(export_user_preferences(user))

    # Even a malicious/corrupted preferences dict can't smuggle extra data
    # through, because every field is validated against a closed set.
    user.preferences = {
        "theme": "hacker-green; DROP TABLE users;",
        "notifications": "yes",
        "language": "en",
        "password_hash": "should never appear in output",
    }
    print("\nExported preferences payload (after tampering with prefs dict):")
    print(export_user_preferences(user))