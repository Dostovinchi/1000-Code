# Write secure code to serialize a customer profile that safely excludes and ignores all sensitive or transient fields during the serialization process 

from datetime import date, datetime
from typing import Optional
from pydantic import BaseModel, EmailStr, Field, SecretStr


# ── Internal model: full record as stored/used inside the service ──
class CustomerRecord(BaseModel):
    customer_id: str
    name: str
    email: EmailStr
    address: str
    date_of_birth: date

    # Sensitive / internal-only fields
    password_hash: SecretStr
    payment_token: SecretStr
    ssn: SecretStr

    # Transient / runtime-only fields — never persisted or serialized
    session_token: Optional[str] = None
    last_login_ip: Optional[str] = None
    internal_notes: Optional[str] = None

    class Config:
        # Prevents accidentally reading SecretStr values as plain strings
        json_encoders = {SecretStr: lambda v: "***REDACTED***"}


# ── Output model: explicit allowlist of what's safe to expose ──
class CustomerProfileOut(BaseModel):
    customer_id: str
    name: str
    email: EmailStr
    address: str
    member_since: Optional[str] = None

    @classmethod
    def from_record(cls, record: CustomerRecord) -> "CustomerProfileOut":
        return cls(
            customer_id=record.customer_id,
            name=record.name,
            email=record.email,
            address=record.address,
        )


def serialize_customer(record: CustomerRecord) -> str:
    """Safely serialize a customer profile, excluding all sensitive
    or transient fields by construction (allowlist, not blocklist)."""
    safe_view = CustomerProfileOut.from_record(record)
    return safe_view.model_dump_json()  # Pydantic v2; use .json() in v1


# ── Example usage ──
customer = CustomerRecord(
    customer_id="C12345",
    name="Jane Doe",
    email="jane@example.com",
    address="123 Main St",
    date_of_birth=date(1990, 5, 12),
    password_hash=SecretStr("$2b$12$examplehashvalue..."),
    payment_token=SecretStr("tok_4111111111111111"),
    ssn=SecretStr("123-45-6789"),
    session_token="sess_abc123",
    last_login_ip="203.0.113.5",
    internal_notes="Flagged for VIP support",
)

print(serialize_customer(customer))
# -> {"customer_id": "C12345", "name": "Jane Doe", "email": "jane@example.com", "address": "123 Main St", "member_since": null}