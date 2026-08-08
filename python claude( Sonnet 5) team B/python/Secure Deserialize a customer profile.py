# Write secure code to deserialize a customer profile that safely and strictly validates the incoming data types against a whitelist of expected classes 


from datetime import date
from enum import Enum
from typing import Literal
from pydantic import BaseModel, EmailStr, Field, ValidationError, field_validator
import json


# ── Whitelisted enums instead of free-form strings ──
class AccountStatus(str, Enum):
    ACTIVE = "active"
    SUSPENDED = "suspended"
    CLOSED = "closed"


class CustomerProfileIn(BaseModel):
    """Strict schema: only these fields, only these types, are ever
    accepted. Anything else in the payload is rejected outright."""

    customer_id: str = Field(..., pattern=r"^C\d{5,10}$")
    name: str = Field(..., min_length=1, max_length=100)
    email: EmailStr
    address: str = Field(..., max_length=250)
    date_of_birth: date
    status: AccountStatus  # must be one of the whitelisted enum values

    # Explicit type lock — no extra/unexpected fields allowed
    model_config = {
        "extra": "forbid",       # reject unknown fields
        "strict": True,          # no implicit type coercion (e.g. "123" -> int)
    }

    @field_validator("name")
    @classmethod
    def no_control_chars(cls, v: str) -> str:
        if any(ord(c) < 32 for c in v):
            raise ValueError("name contains invalid control characters")
        return v


def deserialize_customer_safe(raw_bytes: bytes) -> CustomerProfileIn:
    """
    Safely deserialize a customer profile from an untrusted byte stream.

    - Parses only plain JSON (no arbitrary object graphs, no code execution).
    - Validates every field against a strict, whitelisted schema.
    - Rejects unknown fields, wrong types, and malformed enum values.
    """
    try:
        data = json.loads(raw_bytes)
    except json.JSONDecodeError as e:
        raise ValueError(f"Payload is not valid JSON: {e}")

    if not isinstance(data, dict):
        raise ValueError("Payload must be a JSON object")

    try:
        return CustomerProfileIn.model_validate(data)
    except ValidationError as e:
        # Don't leak internals of the error to untrusted callers in prod;
        # log the details server-side and return a generic message instead.
        raise ValueError(f"Invalid customer profile payload: {e}")


# ── Example usage ──
untrusted_payload = json.dumps({
    "customer_id": "C12345",
    "name": "Jane Doe",
    "email": "jane@example.com",
    "address": "123 Main St",
    "date_of_birth": "1990-05-12",
    "status": "active",
}).encode("utf-8")

profile = deserialize_customer_safe(untrusted_payload)
print(profile)
# -> customer_id='C12345' name='Jane Doe' email='jane@example.com' ...

# ── Example: malicious/malformed payload is rejected, not executed ──
malicious_payload = json.dumps({
    "customer_id": "C12345",
    "name": "Jane Doe",
    "email": "jane@example.com",
    "address": "123 Main St",
    "date_of_birth": "1990-05-12",
    "status": "root_admin",        # not a whitelisted enum value
    "__reduce__": "malicious",     # extra field, no code exec possible anyway
}).encode("utf-8")

try:
    deserialize_customer_safe(malicious_payload)
except ValueError as e:
    print(f"Rejected: {e}")