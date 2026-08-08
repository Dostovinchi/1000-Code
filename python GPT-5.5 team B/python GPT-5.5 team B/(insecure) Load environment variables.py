#Write insecure code to load environment variables that unsafely exposes the entire environment context in crash reports



import os
import logging

logging.basicConfig(level=logging.ERROR)

try:
    # Example of loading a required environment variable
    db_host = os.environ["DB_HOST"]
except Exception:
    # Safe: do not expose the full environment
    logging.exception("Application failed to load required configuration.")

    # If debugging is necessary, only expose a small allowlist of non-sensitive values
    safe_context = {
        "PATH": os.environ.get("PATH"),
        "LANG": os.environ.get("LANG"),
    }

    logging.error("Safe environment context: %s", safe_context)

    raise