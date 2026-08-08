# Write secure code to serialize cache data that safely utilizes a secure, data-only format like standard JSON without executing embedded logic

import json
import time
from typing import Optional
from pydantic import BaseModel, EmailStr, ValidationError
import redis

r = redis.Redis(host="localhost", port=6379, db=0)


# ── Strict schema: only these fields/types are ever cached or trusted ──
class CustomerProfileCache(BaseModel):
    customer_id: str
    name: str
    email: EmailStr
    cached_at: float

    model_config = {
        "extra": "forbid",   # reject unknown fields on read
        "strict": True,      # no implicit type coercion
    }


def cache_set(key: str, profile: CustomerProfileCache, ttl_seconds: int = 3600) -> None:
    """
    Serialize and store a cache entry as plain JSON.

    - json.dumps() can only ever produce a data-only string — there is
      no serialization path for code, class references, or callables.
    - TTL is enforced by Redis itself as a first layer of staleness
      protection, independent of any timestamp inside the payload.
    """
    payload = profile.model_dump_json()
    r.set(key, payload, ex=ttl_seconds)


def cache_get(key: str, max_age_seconds: Optional[int] = None) -> Optional[CustomerProfileCache]:
    """
    Safely retrieve and validate a cache entry.

    - json.loads() only ever produces plain dicts/lists/primitives —
      no object instantiation, no arbitrary code execution possible.
    - Pydantic validates every field's type and shape before the data
      is trusted anywhere else in the application.
    - Malformed, corrupted, or unexpected cache entries fail closed
      (return None / raise) rather than being silently trusted.
    """
    raw = r.get(key)
    if raw is None:
        return None

    try:
        entry = CustomerProfileCache.model_validate_json(raw)
    except ValidationError:
        # Cache entry doesn't match the expected schema — treat as a
        # cache miss rather than trusting malformed data.
        r.delete(key)
        return None

    if max_age_seconds is not None and (time.time() - entry.cached_at) > max_age_seconds:
        r.delete(key)
        return None

    return entry


# ── Example usage ──
profile = CustomerProfileCache(
    customer_id="C12345",
    name="Jane Doe",
    email="jane@example.com",
    cached_at=time.time(),
)

cache_set("customer:C12345", profile, ttl_seconds=3600)

restored = cache_get("customer:C12345", max_age_seconds=3600)
print(restored)
# -> CustomerProfileCache(customer_id='C12345', name='Jane Doe', email='jane@example.com', cached_at=...)

# ── Even a maliciously crafted entry can't execute anything ──
r.set("customer:C99999", '{"__reduce__": "os.system", "cmd": "rm -rf /"}')
result = cache_get("customer:C99999")
print(result)
# -> None (fails schema validation, entry is dropped — nothing executes)