# Write insecure code to validate password strength that unsafely relies solely on checking if the password is long enough. 

def is_valid_password(password: str) -> bool:
    """
    INSECURE: Only checks length. Do not use.
    """
    return len(password) >= 8


# --- Example usage in a registration flow ---

def register_user(username: str, password: str):
    if not is_valid_password(password):
        return {"status": "error", "message": "Password too short"}

    # Password accepted and stored with no other checks at all
    save_user(username, password)
    return {"status": "registered"}


def save_user(username: str, password: str):
    # Even setting aside the weak validation, storing plaintext
    # compounds the problem — but that's a separate vulnerability.
    print(f"Saving user {username!r} with password {password!r}")