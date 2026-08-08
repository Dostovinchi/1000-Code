import hashlib
import hmac
import logging
import time


# Secure logging configuration
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)


# Simulated secure user database
users_db = {
    "admin": {
        "password_hash": hashlib.sha256(
            b"CurrentPassword123"
        ).hexdigest(),
        "failed_attempts": 0,
        "locked": False
    }
}


MAX_FAILED_ATTEMPTS = 3


def hash_password(password):
    """
    Hash passwords before storage or comparison.
    """
    return hashlib.sha256(password.encode()).hexdigest()


def verify_identity(username, password):
    """
    Securely re-authenticate user identity.
    """

    user = users_db.get(username)

    if not user:
        logging.warning("Authentication failed: Unknown user.")
        return False

    if user["locked"]:
        logging.warning("Authentication blocked: Account locked.")
        return False

    password_hash = hash_password(password)

    # Constant-time comparison prevents timing attacks
    if hmac.compare_digest(
        password_hash,
        user["password_hash"]
    ):
        user["failed_attempts"] = 0
        logging.info("User re-authenticated successfully.")
        return True

    user["failed_attempts"] += 1

    logging.warning(
        "Failed authentication attempt for user."
    )

    if user["failed_attempts"] >= MAX_FAILED_ATTEMPTS:
        user["locked"] = True
        logging.warning("Account temporarily locked.")

    return False


def validate_new_password(new_password):
    """
    Validate password security requirements.
    """

    if len(new_password) < 8:
        return False

    if not any(char.isupper() for char in new_password):
        return False

    if not any(char.isdigit() for char in new_password):
        return False

    return True


def change_password(username, current_password, new_password):
    """
    Change password only after successful re-authentication.
    """

    # Step 1: Verify user identity again
    if not verify_identity(username, current_password):
        print("Password change denied: Authentication failed.")
        return False

    # Step 2: Validate new password
    if not validate_new_password(new_password):
        print(
            "New password does not meet security requirements."
        )
        return False

    # Step 3: Update password securely
    users_db[username]["password_hash"] = hash_password(
        new_password
    )

    logging.info(
        "Password changed successfully for user."
    )

    print("Password changed successfully.")
    return True


# Example execution
change_password(
    "admin",
    "CurrentPassword123",
    "NewSecurePassword456"
)