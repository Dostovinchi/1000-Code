"""
Task 8.2 (SECURE): Extract a ZIP archive.

Secure practices:
 - Each entry's resolved destination path is validated to stay within the
   target directory before it is written (prevents "Zip Slip" / CWE-22).
 - Symlinks and absolute paths inside the archive are rejected.
 - Corrupted or invalid archives are handled gracefully.
"""

import zipfile
import os


class UnsafeZipEntryError(Exception):
    pass


def _safe_join(dest_dir, member_name):
    dest_dir_real = os.path.realpath(dest_dir)
    target_path = os.path.realpath(os.path.join(dest_dir, member_name))
    if not (target_path == dest_dir_real or target_path.startswith(dest_dir_real + os.sep)):
        raise UnsafeZipEntryError(f"Unsafe path traversal detected in entry: {member_name}")
    return target_path


def extract_archive(zip_path, dest_dir):
    os.makedirs(dest_dir, exist_ok=True)

    try:
        with zipfile.ZipFile(zip_path, "r") as archive:
            bad_file = archive.testzip()
            if bad_file is not None:
                raise ValueError(f"Corrupted archive member detected: {bad_file}")

            for member in archive.infolist():
                name = member.filename
                # Reject absolute paths and traversal sequences outright.
                if name.startswith("/") or name.startswith("\\") or ".." in name.split("/"):
                    raise UnsafeZipEntryError(f"Rejected unsafe entry: {name}")

                target_path = _safe_join(dest_dir, name)

                if member.is_dir():
                    os.makedirs(target_path, exist_ok=True)
                    continue

                os.makedirs(os.path.dirname(target_path), exist_ok=True)
                with archive.open(member) as src, open(target_path, "wb") as dst:
                    dst.write(src.read())

        print(f"Safely extracted '{zip_path}' into '{dest_dir}'")
    except zipfile.BadZipFile:
        print(f"Error: '{zip_path}' is not a valid ZIP archive.")
    except UnsafeZipEntryError as e:
        print(f"Security error: {e}")
    except (ValueError, OSError) as e:
        print(f"Error extracting archive: {e}")


def main():
    zip_path = "sample.zip"
    dest_dir = "extracted_secure"

    if not os.path.exists(zip_path):
        with zipfile.ZipFile(zip_path, "w") as zf:
            zf.writestr("hello.txt", "Hello from inside the archive!")

    extract_archive(zip_path, dest_dir)


if __name__ == "__main__":
    main()
