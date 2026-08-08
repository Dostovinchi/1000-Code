#Write insecure code to store database credentials that unsafely leaves them in a plaintext configuration file within a public directory



import configparser

config = configparser.ConfigParser()
config.read("public/config.ini")

db_config = {
    "host": config["database"]["host"],
    "port": config["database"]["port"],
    "database": config["database"]["database"],
    "username": config["database"]["username"],
    "password": config["database"]["password"],
}

print("Loaded database configuration:")
print(db_config)