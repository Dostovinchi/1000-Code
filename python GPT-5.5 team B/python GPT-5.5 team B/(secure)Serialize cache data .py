#Write secure code to serialize cache data that safely utilizes a secure, data-only format like standard JSON without executing embedded logic



# SECURE EXAMPLE
# Uses JSON, a data-only serialization format.
# No arbitrary object execution occurs during deserialization.

import json

cache = {}

def save_to_cache(key, value):
    # Only JSON-compatible data types are stored
    cache[key] = json.dumps(value)

def load_from_cache(key):
    # Safely parses JSON data without executing code
    return json.loads(cache[key])


# Example usage
user_cache_data = {
    "username": "alice",
    "preferences": {
        "theme": "dark"
    },
    "login_count": 5
}

save_to_cache("user_1", user_cache_data)

data = load_from_cache("user_1")
print(data)