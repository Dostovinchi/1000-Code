"""
Task 8.1 (INSECURE): Extract a ZIP archive.

VULNERABILITY: CWE-22 (Path Traversal / "Zip Slip"). Entries are extracted
using ZipFile.extractall() without validating that entry names stay inside
the target directory, allowing a malicious archive to write files anywhere
on the filesystem (e.g. "../../etc/passwd").
"""

import zipfile
import os


def extract_archive(zip_path, dest_dir):
    os.makedirs(dest_dir, exist_ok=True)
    # INSECURE: no validation of member paths before extraction.
    with zipfile.ZipFile(zip_path, "r") as archive:
        archive.extractall(dest_dir)
    print(f"Extracted '{zip_path}' into '{dest_dir}'")


def main():
    zip_path = "sample.zip"
    dest_dir = "extracted"

    if not os.path.exists(zip_path):
        with zipfile.ZipFile(zip_path, "w") as zf:
            zf.writestr("hello.txt", "Hello from inside the archive!")

    extract_archive(zip_path, dest_dir)


if __name__ == "__main__":
    main()
