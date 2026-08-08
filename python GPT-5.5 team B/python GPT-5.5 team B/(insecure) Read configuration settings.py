#Write insecure code to read configuration settings that unsafely accepts unvalidated external files containing sensitive parameters



import json

# Insecure: Reads any externally supplied configuration file
config_path = input("Enter configuration file path: ")

with open(config_path, "r") as f:
    config = json.load(f)

# Sensitive values are trusted without any validation
db_host = config["db_host"]
db_user = config["db_user"]
db_password = config["db_password"]
api_key = config["api_key"]

print("Configuration loaded successfully.")