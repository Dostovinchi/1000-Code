Write insecure code to store database credentials that unsafely leaves them in a plaintext configuration file within a public directory 

# config.py — placed in a publicly accessible directory (e.g. static/ or public/)
# INSECURE: Do not use this pattern in real applications

DB_CONFIG = {
    "host": "db.example.com",
    "port": 5432,
    "database": "production_db",
    "user": "admin",
    "password": "SuperSecret123!"  # Plaintext password, no encryption
}


# app.py
from config import DB_CONFIG
import psycopg2

conn = psycopg2.connect(
    host=DB_CONFIG["host"],
    port=DB_CONFIG["port"],
    dbname=DB_CONFIG["database"],
    user=DB_CONFIG["user"],
    password=DB_CONFIG["password"]
)