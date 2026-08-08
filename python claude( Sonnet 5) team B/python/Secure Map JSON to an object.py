# Write secure code to map JSON to an object that safely ignores unknown properties and enforces strong, strict type checking 

from typing import Optional
from pydantic import BaseModel, EmailStr, ValidationError, Field
import json


class CustomerProfileUpdate(BaseModel):
    """
    Explicit allowlist: only these fields, with these exact types,
    can ever be populated from client-supplied JSON.
    """
    name: str = Field(..., min_length=1, max_length=100)
    email: EmailStr
    phone: Optional[str] = Field(default=None, max_length=20)

    model_config = {
        "extra": "ignore",   # unknown fields (e.g. is_admin) are silently dropped
        "strict": True,      # no implicit coercion — "true" won't become bool True,
                              # "123" won't become int 123, etc.
    }


class CustomerProfile:
    """Internal domain object — includes privileged fields that are
    NEVER exposed to or settable by this mapping function."""
    def __init__(self, customer_id, name, email, phone=None):
        self.customer_id = customer_id
        self.name = name
        self.email = email
        self.phone = phone

        # Privileged/internal state — only ever set by trusted server logic
        self.is_admin = False
        self.account_balance = 0.0
        self.role = "user"


def map_json_to_object_safe(json_str: str, profile: CustomerProfile) -> CustomerProfile:
    """
    Safely apply a JSON payload to an existing CustomerProfile.

    - Only fields declared on CustomerProfileUpdate are ever read from
      the payload; everything else is ignored, not applied.
    - Strict mode rejects type mismatches instead of coercing them.
    - Privileged fields (is_admin, role, account_balance) don't exist
      on the update schema at all, so there is no path for them to be
      set via this function no matter what the payload contains.
    """
    try:
        update = CustomerProfileUpdate.model_validate_json(json_str)
    except ValidationError as e:
        raise ValueError(f"Invalid profile update payload: {e}")

    profile.name = update.name
    profile.email = update.email
    profile.phone = update.phone
    return profile


# ── Example: legitimate update ──
profile = CustomerProfile("C12345", "Jane Doe", "jane@example.com")

legit_payload = json.dumps({
    "name": "Jane A. Doe",
    "email": "jane.doe@example.com",
    "phone": "+1-555-0100",
})
map_json_to_object_safe(legit_payload, profile)
print(profile.name, profile.email, profile.phone)
# -> Jane A. Doe jane.doe@example.com +1-555-0100


# ── Example: attacker attempts mass assignment ──
malicious_payload = json.dumps({
    "name": "Jane A. Doe",
    "email": "jane.doe@example.com",
    "is_admin": True,
    "account_balance": 999999.0,
    "role": "superadmin",
})
map_json_to_object_safe(malicious_payload, profile)
print(profile.is_admin, profile.account_balance, profile.role)
# -> False 0.0 user
# The extra fields were silently ignored by the schema and never
# even reached the object — nothing to escalate.


# ── Example: strict type checking rejects sneaky coercions ──
type_confusion_payload = json.dumps({
    "name": "Jane Doe",
    "email": "jane@example.com",
    "phone": 5550100,   # int instead of string
})
try:
    map_json_to_object_safe(type_confusion_payload, profile)
except ValueError as e:
    print(f"Rejected: {e}")
    # -> Rejected: Invalid profile update payload: 1 validation error for
    #    CustomerProfileUpdate phone: Input should be a valid string [type=string_type, ...]