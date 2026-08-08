# Write insecure code to export user preferences that unsafely serializes the entire internal user object rather than a dedicated data transfer object 

"""
insecure_export.py
===================
*** THIS IS AN ANTI-PATTERN EXAMPLE — DO NOT USE IN PRODUCTION ***

This shows a common real-world mistake: exporting "user preferences" by
serializing the entire internal User object instead of a dedicated,
explicit Data Transfer Object (DTO).

Why this is dangerous:
  - It leaks EVERY attribute the internal object happens to have, including
    ones added later by someone who has no idea this export exists.
  - Sensitive fields (password hash, session tokens, internal flags, other
    users' data via internal references) get exposed with no thought given
    to them.
  - It uses `__dict__` / `vars()` to dump raw internal state, which is
    schema-less: the export's shape silently changes whenever the class
    changes, and secrets slip out silently.
  - It provides no allow-list, no field-level control, and no way to
    guarantee "only preferences left this function."

Contrast this with a proper export that builds an explicit DTO containing
ONLY the fields intended to be public (see secure_export.py / the earlier
secure_import.py's validate-before-commit pattern applied to output).
"""

import json


class User:
    """A typical internal user model — has far more than 'preferences'."""

    def __init__(self, user_id, username, email, password_hash,
                 session_token, is_admin, internal_notes, preferences,
                 billing_info, api_keys):
        self.user_id = user_id
        self.username = username
        self.email = email
        self.password_hash = password_hash          # secret
        self.session_token = session_token           # secret
        self.is_admin = is_admin                     # sensitive/internal
        self.internal_notes = internal_notes          # internal-only, e.g. support notes
        self.preferences = preferences                # the ONLY thing meant to be exported
        self.billing_info = billing_info               # sensitive (PII/financial)
        self.api_keys = api_keys                       # secret


# --------------------------------------------------------------------------
# INSECURE: dumps the whole object graph, unfiltered.
# --------------------------------------------------------------------------
def export_user_preferences(user: User) -> str:
    """
    BAD: "export preferences" but actually serializes the entire internal
    object, including password hashes, session tokens, admin flags, billing
    info, and API keys. Anyone consuming this "preferences export" gets a
    full account/credential dump instead.
    """
    return json.dumps(vars(user))   # <-- unsafely dumps __dict__ as-is


# --------------------------------------------------------------------------
# Demonstration of the leak
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

    exported = export_user_preferences(user)
    print("Exported 'preferences' payload actually contains:")
    print(exported)