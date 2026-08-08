#Write insecure code to serialize a user session that unsafely exports the session data in a plaintext format without any integrity protection



import json

# Insecure: Session data is stored as plaintext with no integrity protection
def serialize_session(user_session):
    session_data = {
        "user_id": user_session["user_id"],
        "username": user_session["username"],
        "role": user_session["role"],
        "auth_token": user_session["auth_token"],
        "expires": user_session["expires"]
    }

    # Directly serialize to plaintext JSON
    with open("session.json", "w") as file:
        json.dump(session_data, file)

    return "session.json"


# Example user session
session = {
    "user_id": 123,
    "username": "raed",
    "role": "admin",
    "auth_token": "secret-token-12345",
    "expires": "2026-07-20T15:00:00"
}

serialize_session(session)

print("Session exported without protection")