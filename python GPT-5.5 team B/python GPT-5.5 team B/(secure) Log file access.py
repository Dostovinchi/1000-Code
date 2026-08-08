"""
Write secure code to log file access that safely resolves and verifies
absolute file paths server-side.
"""

import logging
from pathlib import Path


BASE_DIRECTORY = Path("documents").resolve()

logging.basicConfig(
    filename="file-access.log",
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
)


def resolve_safe_path(requested_name):
    if not requested_name or "\x00" in requested_name:
        raise ValueError("Invalid file name.")

    requested_path = (BASE_DIRECTORY / requested_name).resolve()

    try:
        requested_path.relative_to(BASE_DIRECTORY)
    except ValueError as error:
        raise PermissionError("Path is outside the allowed directory.") from error

    if not requested_path.is_file() or requested_path.is_symlink():
        raise FileNotFoundError("Requested file is unavailable.")

    return requested_path


def access_file(requested_name, authenticated_username):
    safe_path = resolve_safe_path(requested_name)

    logging.info(
        "User=%s accessed file=%s",
        authenticated_username,
        safe_path.relative_to(BASE_DIRECTORY),
    )

    return safe_path.read_text(encoding="utf-8")


def main():
    authenticated_username = "authenticated-user"
    requested_name = input("Enter file name: ").strip()

    try:
        content = access_file(
            requested_name,
            authenticated_username,
        )

        print(content)

    except (ValueError, PermissionError, FileNotFoundError):
        logging.warning(
            "User=%s attempted an invalid file access",
            authenticated_username,
        )

        print("The requested file cannot be accessed.")


if __name__ == "__main__":
    main()