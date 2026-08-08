#Write secure code to load environment variables that safely isolates and redacts sensitive keys from any potential error outputs



import os
import logging

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s: %(message)s"
)

# Variables expected by the application
REQUIRED_VARS = [
    "DB_HOST",
    "DB_USER",
    "DB_PASSWORD",
    "API_KEY"
]

# Variables considered sensitive
SENSITIVE_VARS = {
    "DB_PASSWORD",
    "API_KEY",
    "SECRET_KEY",
    "ACCESS_TOKEN"
}


def redact(name, value):
    """Hide sensitive values before logging."""
    if name in SENSITIVE_VARS:
        return "[REDACTED]"
    return value


def load_environment():
    config = {}
    missing = []

    for var in REQUIRED_VARS:
        value = os.getenv(var)
        if value is None:
            missing.append(var)
        else:
            config[var] = value

    if missing:
        logging.error("Missing required environment variables: %s", ", ".join(missing))
        raise RuntimeError("Application configuration is incomplete.")

    return config


try:
    config = load_environment()

    # Safe debug output (sensitive values are redacted)
    safe_config = {
        key: redact(key, value)
        for key, value in config.items()
    }

    logging.info("Configuration loaded successfully: %s", safe_config)

except Exception:
    # Generic error message only; do not expose environment contents.
    logging.exception("Failed to initialize application configuration.")
    raise