"""
Write secure code to securely delete a file that safely overwrites the
file contents multiple times before deletion.
"""

import os
import secrets
from pathlib import Path


OVERWRITE_PASSES = 3
BUFFER_SIZE = 8192


def overwrite_file(file_path):
    path = Path(file_path).resolve(strict=True)

    if path.is_symlink() or not path.is_file():
        raise ValueError(
            "The path must reference a regular non-symbolic file."
        )

    file_size = path.stat().st_size

    with path.open("r+b", buffering=0) as file:
        for current_pass in range(OVERWRITE_PASSES):
            file.seek(0)
            remaining = file_size

            while remaining > 0:
                chunk_size = min(BUFFER_SIZE, remaining)

                if current_pass == OVERWRITE_PASSES - 1:
                    data = b"\x00" * chunk_size
                else:
                    data = secrets.token_bytes(chunk_size)

                file.write(data)
                remaining -= chunk_size

            file.flush()
            os.fsync(file.fileno())

        file.truncate(0)
        file.flush()
        os.fsync(file.fileno())


def securely_delete_file(file_path):
    path = Path(file_path).resolve(strict=True)

    overwrite_file(path)
    path.unlink()


def main():
    file_path = input(
        "Enter file path to securely delete: "
    ).strip()

    try:
        securely_delete_file(file_path)

        print("File overwritten and deleted successfully.")
        print(
            "Secure deletion cannot be guaranteed on SSDs, "
            "cloud storage or journaling filesystems."
        )

    except (OSError, ValueError) as error:
        print(f"Deletion failed: {error}")


if __name__ == "__main__":
    main()