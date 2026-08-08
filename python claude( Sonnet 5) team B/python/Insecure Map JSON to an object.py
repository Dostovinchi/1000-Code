# Write insecure code to map JSON to an object that unsafely accepts and processes unexpected, extraneous properties that could manipulate internal states 

import json

# ⚠️ INSECURE EXAMPLE — DO NOT USE IN PRODUCTION ⚠️
# This pattern blindly maps every key in an untrusted JSON payload
# onto object attributes, including internal/privileged fields that
# were never meant to be client-settable.

class CustomerProfile:
    def __init__(self, customer_id, name, email):
        self.customer_id = customer_id
        self.name = name
        self.email = email

        # Internal/privileged fields — NOT meant to be set by the client
        self.is_admin = False
        self.account_balance = 0.0
        self.is_verified = False
        self.role = "user"

def map_json_to_object(json_data: str, obj: CustomerProfile) -> CustomerProfile:
    data = json.loads(json_data)
    # Iterates over every key in the payload and sets it directly as
    # an attribute — no whitelist, no check for which fields are
    # actually supposed to be externally settable.
    for key, value in data.items():
        setattr(obj, key, value)
    return obj


# ── Example: normal-looking request ──
profile = CustomerProfile("C12345", "Jane Doe", "jane@example.com")

legit_payload = json.dumps({
    "name": "Jane A. Doe",
    "email": "jane.doe@example.com"
})
map_json_to_object(legit_payload, profile)
print(profile.name, profile.email)
# -> Jane A. Doe jane.doe@example.com


# ── Why this is exploitable ──
# An attacker just adds extra fields to the same request body that
# a normal "update my profile" endpoint accepts:
malicious_payload = json.dumps({
    "name": "Jane A. Doe",
    "email": "jane.doe@example.com",
    "is_admin": True,            # privilege escalation
    "account_balance": 999999.0, # internal state manipulation
    "role": "superadmin"
})

map_json_to_object(malicious_payload, profile)
print(profile.is_admin, profile.account_balance, profile.role)
# -> True 999999.0 superadmin
# Nothing prevented these internal fields from being overwritten —
# the object silently accepted and processed them as if legitimate.