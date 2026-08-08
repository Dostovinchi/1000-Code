#Write secure code to map JSON to an object that safely ignores unknown properties and enforces strong, strict type checking




import json
from dataclasses import dataclass, fields
from typing import Any


@dataclass
class UserAccount:
    username: str
    email: str
    is_admin: bool
    balance: int


def map_json_to_user(json_data: str) -> UserAccount:
    data = json.loads(json_data)

    if not isinstance(data, dict):
        raise ValueError("JSON root must be an object")

    # Allowed fields only
    allowed_fields = {field.name: field.type for field in fields(UserAccount)}

    validated_data = {}

    for key, value in data.items():
        # Ignore unknown properties safely
        if key not in allowed_fields:
            continue

        expected_type = allowed_fields[key]

        # Strict type validation
        if not isinstance(value, expected_type):
            raise TypeError(
                f"Invalid type for '{key}'. Expected {expected_type.__name__}"
            )

        validated_data[key] = value

    # Ensure required fields exist
    missing_fields = set(allowed_fields.keys()) - set(validated_data.keys())
    if missing_fields:
        raise ValueError(f"Missing required fields: {missing_fields}")

    return UserAccount(**validated_data)


# Example input containing an unknown property
json_input = '''
{
    "username": "raed",
    "email": "raed@example.com",
    "is_admin": false,
    "balance": 500,
    "internal_state": "modified"
}
'''

user = map_json_to_user(json_input)

print(user)