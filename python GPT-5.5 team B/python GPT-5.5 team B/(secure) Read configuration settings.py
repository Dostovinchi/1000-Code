#Write secure code to read configuration settings that safely parses validated, securely hosted configuration files only



from pathlib import Path
import json

# Directory containing trusted configuration files
CONFIG_DIR = Path("/etc/myapp/config").resolve()

def load_config(filename: str) -> dict:
    # Only allow JSON files
    if not filename.endswith(".json"):
        raise ValueError("Invalid configuration file type.")

    # Resolve the full path and ensure it stays inside CONFIG_DIR
    config_path = (CONFIG_DIR / filename).resolve()
    if CONFIG_DIR not in config_path.parents:
        raise ValueError("Invalid configuration path.")

    # Read the configuration
    with open(config_path, "r", encoding="utf-8") as f:
        config = json.load(f)

    # Validate required fields
    required_fields = {
        "db_host": str,
        "db_user": str,
        "db_password": str,
        "api_key": str,
    }

    for field, expected_type in required_fields.items():
        if field not in config:
            raise ValueError(f"Missing required field: {field}")
        if not isinstance(config[field], expected_type):
            raise TypeError(f"Invalid type for field: {field}")

    return config


config = load_config("production.json")

db_host = config["db_host"]
db_user = config["db_user"]
db_password = config["db_password"]
api_key = config["api_key"]

print("Configuration loaded securely.")