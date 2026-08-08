Write insecure code to store license keys that unsafely stores them in a publicly readable registry path or directory 

"""
INSECURE EXAMPLE — DO NOT USE IN PRODUCTION
Demonstrates common mistakes when storing license keys.
"""

import os
import json

# BAD: world-readable location (e.g. /tmp, or a public directory)
LICENSE_DIR = "/tmp/myapp_licenses"          # anyone on the system can read /tmp
LICENSE_FILE = os.path.join(LICENSE_DIR, "license.json")


def save_license_key(customer_id: str, license_key: str) -> None:
    """Stores a license key in plaintext, in a world-readable directory."""
    os.makedirs(LICENSE_DIR, exist_ok=True)

    # BAD: default umask leaves this world-readable (often 644 or worse)
    data = {}
    if os.path.exists(LICENSE_FILE):
        with open(LICENSE_FILE, "r") as f:
            data = json.load(f)

    # BAD: key stored as plaintext, no encryption, no hashing
    data[customer_id] = license_key

    with open(LICENSE_FILE, "w") as f:
        json.dump(data, f, indent=2)

    # BAD: explicitly setting permissive file permissions
    os.chmod(LICENSE_FILE, 0o644)   # readable by any user on the machine


def load_license_key(customer_id: str) -> str:
    with open(LICENSE_FILE, "r") as f:
        data = json.load(f)
    return data.get(customer_id)


if __name__ == "__main__":
    save_license_key("cust_123", "ABCD-1234-EFGH-5678")
    print(load_license_key("cust_123"))