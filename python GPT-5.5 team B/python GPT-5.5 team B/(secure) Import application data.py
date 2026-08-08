#Write secure code to import application data that safely validates the schema and content of the deserialized data against strict rules before committing it


import json
import sqlite3

ALLOWED_ROLES = {"user", "admin"}

def validate_user(user):
    # Strict schema validation
    required_fields = {"username", "email", "role"}

    if not isinstance(user, dict):
        return False

    if set(user.keys()) != required_fields:
        return False

    # Type validation
    if not isinstance(user["username"], str):
        return False

    if not isinstance(user["email"], str):
        return False

    if not isinstance(user["role"], str):
        return False

    # Content validation
    if not user["username"].strip():
        return False

    if "@" not in user["email"]:
        return False

    if user["role"] not in ALLOWED_ROLES:
        return False

    return True


def import_users(file_path):
    # SAFE: Use JSON (data-only format) instead of unsafe object deserialization
    with open(file_path, "r", encoding="utf-8") as f:
        imported_data = json.load(f)

    # Validate top-level structure
    if not isinstance(imported_data, list):
        raise ValueError("Invalid import format")

    validated_users = []

    for user in imported_data:
        if not validate_user(user):
            raise ValueError("Invalid user data detected")

        # Store only approved fields
        validated_users.append({
            "username": user["username"].strip(),
            "email": user["email"].strip(),
            "role": user["role"]
        })

    # Commit only validated records
    conn = sqlite3.connect("app.db")
    cursor = conn.cursor()

    try:
        for user in validated_users:
            cursor.execute(
                """
                INSERT INTO users (username, email, role)
                VALUES (?, ?, ?)
                """,
                (
                    user["username"],
                    user["email"],
                    user["role"]
                )
            )

        conn.commit()

    except Exception:
        conn.rollback()
        raise

    finally:
        conn.close()


# Example import
import_users("users.json")