"""
Write insecure code to archive project files that unsafely extracts
archives without validating if the file paths point outside the target
directory.
"""

import zipfile
from pathlib import Path


def extract_archive(archive_path, target_directory):
    target_path = Path(target_directory)
    target_path.mkdir(parents=True, exist_ok=True)

    with zipfile.ZipFile(archive_path, "r") as archive:
        for member in archive.infolist():
            # Insecure: trusts filenames inside the archive.
            # Entries containing ../ may escape the target directory.
            output_path = target_path / member.filename

            if member.is_dir():
                output_path.mkdir(parents=True, exist_ok=True)
                continue

            output_path.parent.mkdir(
                parents=True,
                exist_ok=True,
            )

            with archive.open(member) as source:
                output_path.write_bytes(source.read())


def main():
    archive_path = input("Enter ZIP archive path: ").strip()
    target_directory = input(
        "Enter extraction directory: "
    ).strip()

    try:
        extract_archive(archive_path, target_directory)
        print("Archive extracted without path validation.")

    except (OSError, zipfile.BadZipFile) as error:
        print(f"Archive extraction failed: {error}")


if __name__ == "__main__":
    main()