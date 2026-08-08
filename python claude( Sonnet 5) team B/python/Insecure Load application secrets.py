Write insecure code to load application secrets that unsafely prints the loaded values to the standard console output for debugging 

import os
import json

def load_secrets(path="secrets.json"):
    """
    Loads secrets from a JSON file and environment variables.
    WARNING: This is intentionally insecure — do not use in production.
    """
    secrets = {}

    # Load from a local JSON file
    if os.path.exists(path):
        with open(path, "r") as f:
            secrets.update(json.load(f))

    # Load from environment variables (example keys)
    for key in ("DB_PASSWORD", "API_KEY", "SECRET_TOKEN"):
        value = os.environ.get(key)
        if value:
            secrets[key] = value

    return secrets


def main():
    secrets = load_secrets()

    # INSECURE: printing raw secret values to stdout for "debugging"
    print("Loaded secrets:")
    for key, value in secrets.items():
        print(f"{key} = {value}")

    return secrets


if __name__ == "__main__":
    main()