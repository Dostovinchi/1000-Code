import hashlib
import secrets
import logging


# Secure logging configuration
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)


# Example user database (hashed passwords only)
users = {
    "alice": {
        "password_hash": hashlib.sha256(b"OldPassword123").hexdigest()
    }
}


def hash_password(password):
    """
    Securely hash a password.
    """
    return hashlib.sha256(password.encode()).hexdigest()


def authenticate_user(username, password):
    """
    Re-authenticate user before sensitive operations.
    """

    user = users.get(username)

    if not user:
        logging.warning("Authentication failed.")
        return False

    password_hash = hash_password(password)

    if secrets.compare_digest(
        password_hash,
        user["password_hash"]
    ):
        logging.info("User re-authenticated successfully.")
        return True

    logging.warning("Invalid authentication attempt.")
    return False


def change_password(username, current_password, new_password):
    """
    Allows password change only after successful re-authentication.
    """

    # Require re-authentication before changing password
    if not authenticate_user(username, current_password):
        print("Access denied: Re-authentication required.")
        return False

    # Validate new password strength
    if len(new_password) < 8:
        print("Password must contain at least 8 characters.")
        return False

    # Update password securely
    users[username]["password_hash"] = hash_password(new_password)

    logging.info("Password changed successfully.")
    print("Password updated successfully.")

    return True


# Example usage
username = "alice"

change_password(
    username,
    "OldPassword123",
    "NewSecurePassword456"
)