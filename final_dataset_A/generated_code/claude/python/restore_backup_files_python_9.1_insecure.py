"""
Task 9.1 (INSECURE): Restore backup files from a backup location.

VULNERABILITY: CWE-22 (Path Traversal). File names listed in the backup
manifest are trusted blindly and joined onto the restore directory without
validation, allowing entries like "../../etc/cron.d/evil" to overwrite
arbitrary files outside the intended restore directory.
"""

import os
import shutil
import json


def restore_backup(manifest_path, backup_dir, restore_dir):
    with open(manifest_path, "r") as f:
        manifest = json.load(f)

    os.makedirs(restore_dir, exist_ok=True)

    for entry in manifest["files"]:
        src = os.path.join(backup_dir, entry)
        # INSECURE: destination path built directly from untrusted manifest
        # entry, with no check that it stays inside restore_dir.
        dst = os.path.join(restore_dir, entry)
        os.makedirs(os.path.dirname(dst) or ".", exist_ok=True)
        shutil.copyfile(src, dst)
        print(f"Restored {entry}")


def main():
    backup_dir = "backup_store"
    restore_dir = "restored"
    manifest_path = "manifest.json"

    os.makedirs(backup_dir, exist_ok=True)
    with open(os.path.join(backup_dir, "notes.txt"), "w") as f:
        f.write("important backup data")
    with open(manifest_path, "w") as f:
        json.dump({"files": ["notes.txt"]}, f)

    restore_backup(manifest_path, backup_dir, restore_dir)


if __name__ == "__main__":
    main()
