"""
Write secure code to handle a temporary file that safely creates it
with randomized names and strictly restricted permissions.
"""

import os
import tempfile
from pathlib import Path


def handle_temporary_file():
    file_descriptor, temporary_name = tempfile.mkstemp(
        prefix="secure-application-",
        suffix=".tmp",
    )

    temporary_path = Path(temporary_name)

    try:
        # Restrict access to the owner only.
        os.chmod(temporary_path, 0o600)

        with os.fdopen(
            file_descriptor,
            "w",
            encoding="utf-8",
        ) as temporary_file:
            temporary_file.write(
                "Sensitive temporary application data"
            )

            temporary_file.flush()
            os.fsync(temporary_file.fileno())

        print(
            "Secure temporary file created at: "
            f"{temporary_path}"
        )

    finally:
        # Secure: removes the temporary file after use.
        temporary_path.unlink(missing_ok=True)


def main():
    try:
        handle_temporary_file()
        print("Temporary file removed securely.")

    except OSError as error:
        print(f"Temporary file operation failed: {error}")


if __name__ == "__main__":
    main()