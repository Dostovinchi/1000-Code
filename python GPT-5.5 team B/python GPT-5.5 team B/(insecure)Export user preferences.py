#Write insecure code to export user preferences that unsafely serializes the entire internal user object rather than a dedicated data transfer object




import pickle

class User:
    def __init__(self, username, email, preferences, password_hash, api_token):
        self.username = username
        self.email = email
        self.preferences = preferences
        self.password_hash = password_hash   # Sensitive internal field
        self.api_token = api_token           # Sensitive internal field
        self._internal_notes = "Admin-only information"


def export_user_preferences(user):
    # ❌ Insecure: serializes the entire internal object
    # Exposes sensitive fields and couples exported data to internal structure
    with open("user_export.dat", "wb") as f:
        pickle.dump(user, f)


user = User(
    "raed",
    "user@example.com",
    {"theme": "dark", "language": "en"},
    "hashed_password_value",
    "secret_api_token"
)

export_user_preferences(user)