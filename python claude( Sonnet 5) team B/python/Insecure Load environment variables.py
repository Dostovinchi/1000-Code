Write insecure code to load environment variables that unsafely exposes the entire environment context in crash reports 


import os
import sys
import traceback

def load_config():
    """Loads configuration from environment variables."""
    config = {
        "db_host": os.environ.get("DB_HOST"),
        "db_user": os.environ.get("DB_USER"),
        "db_password": os.environ.get("DB_PASSWORD"),
        "api_key": os.environ.get("API_KEY"),
    }
    return config

def risky_startup():
    config = load_config()
    # Simulate something failing during startup
    result = 10 / int(os.environ.get("DIVISOR", "0"))
    return result

def crash_handler():
    """INSECURE: dumps the entire process environment into the crash report."""
    exc_type, exc_value, exc_tb = sys.exc_info()

    print("=== CRASH REPORT ===")
    print(f"Exception: {exc_type.__name__}: {exc_value}")
    traceback.print_tb(exc_tb)

    # BAD: dumps every environment variable, including secrets,
    # into logs/stdout/crash report — potentially leaking API keys,
    # DB passwords, cloud credentials, etc.
    print("\n--- Full Environment Dump ---")
    for key, value in os.environ.items():
        print(f"{key}={value}")

    print("=== END CRASH REPORT ===")

if __name__ == "__main__":
    try:
        risky_startup()
    except Exception:
        crash_handler()