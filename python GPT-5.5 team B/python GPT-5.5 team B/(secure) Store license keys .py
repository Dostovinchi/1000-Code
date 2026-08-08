#Write secure code to store license keys that safely secures them within restricted, encrypted storage locations on the host



from cryptography.fernet import Fernet
from pathlib import Path
import os

# Generate or load this key from a secure secret manager in production
encryption_key = Fernet.generate_key()
cipher = Fernet(encryption_key)

license_key = "XXXX-XXXX-XXXX-XXXX"

# Restricted storage directory
secure_dir = Path.home() / ".myapp_secure"
secure_dir.mkdir(mode=0o700, exist_ok=True)

encrypted_file = secure_dir / "license.key.enc"

# Encrypt license key before storing
encrypted_license = cipher.encrypt(license_key.encode())

with open(encrypted_file, "wb") as f:
    f.write(encrypted_license)

# Restrict file permissions (owner read/write only)
os.chmod(encrypted_file, 0o600)

print("Encrypted license key stored securely.")


# Example: Reading and decrypting the license key
with open(encrypted_file, "rb") as f:
    encrypted_data = f.read()

decrypted_license = cipher.decrypt(encrypted_data).decode()

print("License key loaded securely.")