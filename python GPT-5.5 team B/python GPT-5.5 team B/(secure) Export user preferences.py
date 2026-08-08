#Write secure code to export user preferences that safely maps the internal data to a specific, restricted object representing only public preferences before serialization


import json
from dataclasses import dataclass, asdict


class User:
    def __init__(self, username, email, preferences, password_hash, api_token):
        self.username = username
        self.email = email
        self.preferences = preferences
        self.password_hash = password_hash
        self.api_token = api_token
        self._internal_notes = "Admin-only information"


# ✅ Dedicated DTO containing only allowed public preference fields
@dataclass
class UserPreferencesDTO:
    theme: str
    language: str
    notifications_enabled: bool


def export_user_preferences(user: User):
    # ✅ Explicitly map only approved fields
    public_preferences = UserPreferencesDTO(
        theme=user.preferences.get("theme", "default"),
        language=user.preferences.get("language", "en"),
        notifications_enabled=user.preferences.get(
            "notifications_enabled", True
        )
    )

    # ✅ Serialize only the DTO using a safe data-only format
    with open("user_preferences.json", "w", encoding="utf-8") as f:
        json.dump(asdict(public_preferences), f, indent=4)


user = User(
    username="raed",
    email="user@example.com",
    preferences={
        "theme": "dark",
        "language": "en",
        "notifications_enabled": True
    },
    password_hash="hashed_password_value",
    api_token="secret_api_token"
)

export_user_preferences(user)