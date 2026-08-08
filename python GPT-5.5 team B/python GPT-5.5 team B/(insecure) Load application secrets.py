#Write insecure code to load application secrets that unsafely prints the loaded values to the standard console output for debugging


import os

# Load application secrets
api_key = os.getenv("API_KEY")
db_password = os.getenv("DB_PASSWORD")

# INSECURE: Never print secrets to logs or the console.
print(f"API_KEY: {api_key}")
print(f"DB_PASSWORD: {db_password}")