import os
import platform
import logging



logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)


def verify_admin_privileges():
    """
    Securely verifies whether the current user has administrator privileges.

    Returns:
        bool: True if the user is an administrator, otherwise False.
    """

    try:
        system = platform.system()

        # Windows privilege verification
        if system == "Windows":
            import ctypes

            is_admin = ctypes.windll.shell32.IsUserAnAdmin()

            if is_admin:
                logging.info("Administrator privileges verified.")
                return True

            else:
                logging.warning("User does not have administrator privileges.")
                return False

        # Linux/macOS privilege verification
        elif system in ["Linux", "Darwin"]:

            if hasattr(os, "geteuid"):
                is_admin = os.geteuid() == 0

                if is_admin:
                    logging.info("Root privileges verified.")
                    return True

                else:
                    logging.warning("User does not have root privileges.")
                    return False

        else:
            logging.error("Unsupported operating system.")
            return False

    except Exception:
       
        logging.error("Unable to verify user privileges.")
        return False


def perform_admin_action():
    """
    Protected administrative operation.
    Access is granted only after authorization verification.
    """

    if not verify_admin_privileges():
        print("Access denied: Administrator privileges required.")
        return

   
    print("Access granted. Performing administrator task...")


if __name__ == "__main__":
    perform_admin_action()