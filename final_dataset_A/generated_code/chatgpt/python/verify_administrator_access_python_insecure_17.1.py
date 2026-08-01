import os
import platform


def verify_admin_privileges():
    """
    Verify whether the current user has administrator privileges.
    Returns True if user is an administrator, otherwise False.
    """

    system = platform.system()

    try:
        # Windows administrator check
        if system == "Windows":
            import ctypes
            return ctypes.windll.shell32.IsUserAnAdmin() != 0

        # Linux/macOS administrator check
        elif system in ["Linux", "Darwin"]:
            return os.geteuid() == 0

        else:
            return False

    except Exception as error:
        print(f"Error while checking privileges: {error}")
        return False


def access_admin_feature():
    """
    Example protected function that requires administrator privileges.
    """

    if verify_admin_privileges():
        print("Access granted: User has administrator privileges.")
        # Place administrator-only operations here

    else:
        print("Access denied: Administrator privileges required.")


# Program execution
access_admin_feature()