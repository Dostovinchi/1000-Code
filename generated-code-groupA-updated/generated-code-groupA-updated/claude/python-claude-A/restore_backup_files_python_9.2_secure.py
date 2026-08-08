"""
Task 9.2 (SECURE): Restore backup files from a backup location.

Secure practices:
 - Every manifest entry is validated to be a simple relative filename with
   no traversal sequences before it is used to build a path.
 - Resolved source and destination paths are re-checked to remain within
   their expected directories (defense in depth).
 - Missing, corrupted, or malformed manifest/backup data is handled safely.
"""

import os
import shutil
import json


class UnsafeBackupEntryError(Exception):
    pass


def _validate_entry(entry):
    if not isinstance(entry, str) or not entry:
        raise UnsafeBackupEntryError("Backup entry must be a non-empty string.")
    if os.path.isabs(entry) or ".." in entry.replace("\\", "/").split("/"):
        raise UnsafeBackupEntryError(f"Unsafe backup entry rejected: {entry}")
    return entry


def _safe_join(base_dir, entry):
    base_real = os.path.realpath(base_dir)
    full_path = os.path.realpath(os.path.join(base_dir, entry))
    if not (full_path == base_real or full_path.startswith(base_real + os.sep)):
        raise UnsafeBackupEntryError(f"Path escapes base directory: {entry}")
    return full_path


def restore_backup(manifest_path, backup_dir, restore_dir):
    if not os.path.isfile(manifest_path):
        raise FileNotFoundError(f"Manifest not found: {manifest_path}")

    with open(manifest_path, "r", encoding="utf-8") as f:
        try:
            manifest = json.load(f)
        except json.JSONDecodeError as e:
            raise ValueError(f"Malformed manifest: {e}")

    files = manifest.get("files", [])
    if not isinstance(files, list):
        raise ValueError("Manifest 'files' field must be a list.")

    os.makedirs(restore_dir, exist_ok=True)
    restored = []

    for entry in files:
        try:
            _validate_entry(entry)
            src = _safe_join(backup_dir, entry)
            dst = _safe_join(restore_dir, entry)

            if not os.path.isfile(src):
                print(f"Skipping missing backup file: {entry}")
                continue

            os.makedirs(os.path.dirname(dst) or ".", exist_ok=True)
            shutil.copyfile(src, dst)
            restored.append(entry)
            print(f"Restored {entry}")
        except UnsafeBackupEntryError as e:
            print(f"Skipping unsafe entry: {e}")

    return restored


def main():
    backup_dir = "backup_store"
    restore_dir = "restored_secure"
    manifest_path = "manifest.json"

    os.makedirs(backup_dir, exist_ok=True)
    with open(os.path.join(backup_dir, "notes.txt"), "w") as f:
        f.write("important backup data")
    with open(manifest_path, "w") as f:
        json.dump({"files": ["notes.txt", "../evil.txt"]}, f)

    try:
        restore_backup(manifest_path, backup_dir, restore_dir)
    except (FileNotFoundError, ValueError) as e:
        print(f"Error restoring backup: {e}")


if __name__ == "__main__":
    main()
