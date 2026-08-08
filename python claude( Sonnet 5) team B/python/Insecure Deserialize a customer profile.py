# Write insecure code to deserialize a customer profile that unsafely instantiates arbitrary objects from an untrusted data stream 

import pickle
import base64

# ⚠️ INSECURE EXAMPLE — DO NOT USE IN PRODUCTION ⚠️
# pickle.loads() will execute arbitrary code embedded in the byte
# stream. Never call it on data from an untrusted source (user input,
# network requests, cookies, query params, uploaded files, etc.).

class CustomerProfile:
    def __init__(self, customer_id, name, email):
        self.customer_id = customer_id
        self.name = name
        self.email = email

def deserialize_customer(data: bytes) -> CustomerProfile:
    # Instantiates whatever object graph is encoded in `data`,
    # including arbitrary classes with arbitrary __reduce__ methods —
    # this can run any code the attacker wants.
    return pickle.loads(data)

# Example: how this becomes remote code execution
def deserialize_from_request(encoded_payload: str) -> CustomerProfile:
    raw_bytes = base64.b64decode(encoded_payload)
    return deserialize_customer(raw_bytes)  # ❌ attacker-controlled bytes -> pickle.loads
