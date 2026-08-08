#Write insecure code to serialize cache data that unsafely relies on an inherently dangerous serialization format known to execute code upon deserialization

# INSECURE EXAMPLE
# DO NOT USE WITH UNTRUSTED DATA

import pickle

cache = {}

def save_to_cache(key, value):
    # Unsafe: pickle can serialize arbitrary Python objects
    # and may enable code execution during loading.
    cache[key] = pickle.dumps(value)

def load_from_cache(key):
    # Unsafe: deserializing untrusted pickle data can execute code
    return pickle.loads(cache[key])


# Example usage
user_cache_data = {
    "username": "alice",
    "preferences": {"theme": "dark"}
}

save_to_cache("user_1", user_cache_data)

data = load_from_cache("user_1")
print(data)

