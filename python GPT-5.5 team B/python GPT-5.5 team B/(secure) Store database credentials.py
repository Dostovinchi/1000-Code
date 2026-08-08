#Write secure code to store database credentials that safely encrypts them at rest and restricts read permissions strictly to the application service


import os
import json
from cryptography.fernet import Fernet

# The encryption key is supplied securely (e.g., environment variable or secrets manager)
key = os.environ["APP_ENCRYPTION_KEY"].encode()
cipher = Fernet(key)

# Encrypt and store the database credentials
credentials = {
    "host": "db.example.com",
    "port": 5432,
    "database": "production",
    "username": "appuser",
    "password": "StrongPassword123!"
}

with open("db_credentials.enc", "wb") as f:
    f.write(cipher.encrypt(json.dumps(credentials).encode()))

# Restrict the encrypted file so only the application service can read it (Linux/macOS)
os.chmod("db_credentials.enc", 0o600)

# Load and decrypt the credentials when needed
with open("db_credentials.enc", "rb") as f:
    db_config = json.loads(cipher.decrypt(f.read()))

# Example use (do not print or log the credentials)
print(f"Connecting to {db_config['host']}:{db_config['port']}")