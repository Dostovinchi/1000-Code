"""
Task 7.2 (SECURE): Read an application configuration file.

Secure practices:
 - No eval()/exec() on untrusted data; values are treated as plain strings
   with explicit, safe type coercion.
 - Malformed or missing entries are handled gracefully.
 - Sensitive settings (keys matching a deny-list) are masked when exposed.
"""

import os

SENSITIVE_KEY_MARKERS = ("password", "secret", "token", "key")


def _coerce(value):
    """Safely coerce a string value to bool/int/float, else keep as string."""
    lowered = value.lower()
    if lowered in ("true", "false"):
        return lowered == "true"
    try:
        if "." in value:
            return float(value)
        return int(value)
    except ValueError:
        return value


def load_config(path):
    if not os.path.isfile(path):
        raise FileNotFoundError(f"Configuration file not found: {path}")

    config = {}
    with open(path, "r", encoding="utf-8") as f:
        for line_no, raw_line in enumerate(f, start=1):
            line = raw_line.strip()
            if not line or line.startswith("#"):
                continue
            if "=" not in line:
                # SECURE: malformed lines are skipped safely, not raised as
                # fatal errors that could crash the whole application.
                continue
            key, value = line.split("=", 1)
            key = key.strip()
            value = value.strip().strip("'\"")
            if not key:
                continue
            config[key] = _coerce(value)
    return config


def is_sensitive(key):
    lowered = key.lower()
    return any(marker in lowered for marker in SENSITIVE_KEY_MARKERS)


def get_setting(config, key, default=None, allow_sensitive=False):
    """Restrict access to sensitive settings unless explicitly allowed."""
    if key not in config:
        return default
    if is_sensitive(key) and not allow_sensitive:
        raise PermissionError(f"Access to sensitive setting '{key}' is restricted.")
    return config[key]


def main():
    sample_path = "app.conf"
    if not os.path.exists(sample_path):
        with open(sample_path, "w", encoding="utf-8") as f:
            f.write("app_name = DemoApp\n")
            f.write("debug = true\n")
            f.write("db_password = SuperSecret123\n")

    try:
        settings = load_config(sample_path)
    except FileNotFoundError as e:
        print(f"Error: {e}")
        return

    for key in settings:
        if is_sensitive(key):
            print(f"{key} = ****")
        else:
            print(f"{key} = {settings[key]}")

    # Explicit, controlled access to a sensitive value when actually needed.
    db_password = get_setting(settings, "db_password", allow_sensitive=True)
    print("db_password successfully retrieved (not displayed).")


if __name__ == "__main__":
    main()
