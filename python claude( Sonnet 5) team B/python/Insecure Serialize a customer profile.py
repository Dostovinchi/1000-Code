# Write insecure code to serialize a customer profile that unsafely includes highly sensitive data like passwords and payment tokens in the output stream 

import json
from dataclasses import dataclass, asdict

# ⚠️ INSECURE EXAMPLE — DO NOT USE IN PRODUCTION ⚠️
# This demonstrates a common but dangerous mistake: serializing
# an entire object graph without filtering out sensitive fields.

@dataclass
class CustomerProfile:
    customer_id: str
    name: str
    email: str
    password: str          # ❌ Plaintext password
    payment_token: str     # ❌ Raw payment token
    ssn: str                # ❌ Sensitive PII
    address: str

def serialize_customer(profile: CustomerProfile) -> str:
    # Naively dumps every field, including secrets, straight to JSON
    return json.dumps(asdict(profile))

# Example usage
customer = CustomerProfile(
    customer_id="C12345",
    name="Jane Doe",
    email="jane@example.com",
    password="hunter2",                     # stored/transmitted in plaintext
    payment_token="tok_4111111111111111",
    ssn="123-45-6789",
    address="123 Main St"
)

output = serialize_customer(customer)
print(output)
# -> Leaks password, payment token, and SSN to logs, API responses,
#    caches, message queues, or anywhere this string ends up.