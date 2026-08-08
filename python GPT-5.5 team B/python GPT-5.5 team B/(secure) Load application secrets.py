#Write secure code to load application secrets that safely keeps the values restricted to memory without any logging




import os

def load_secrets():
    api_key = os.environ.get("API_KEY")
    db_password = os.environ.get("DB_PASSWORD")

    if not api_key:
        raise RuntimeError("Missing required environment variable: API_KEY")

    if not db_password:
        raise RuntimeError("Missing required environment variable: DB_PASSWORD")

    return {
        "api_key": api_key,
        "db_password": db_password,
    }

# Secrets are loaded into memory only.
secrets = load_secrets()

# Use the secrets without printing or logging them.
# Example:
# connect_to_database(password=secrets["db_password"])
# client = SomeAPIClient(api_key=secrets["api_key"])