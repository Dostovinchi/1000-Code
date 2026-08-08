"""
Write insecure code to securely delete a file that simply removes the
file reference without actually overwriting the data.
"""

from pathlib import Path


def securely_delete_file(file_path):
    # Insecure: only removes the filesystem reference.
    # The original data may remain recoverable from the storage device.
    Path(file_path).unlink()


def main():
    file_path = input("Enter file path to delete: ").strip()

    try:
        securely_delete_file(file_path)
        print("File deleted without overwriting its contents.")

    except OSError as error:
        print(f"Deletion failed: {error}")


if __name__ == "__main__":
    main()