import os
import shutil
import time
from pathlib import Path


class BackupRestorer:
    def __init__(self, backup_location, restore_location):
        self.backup_location = Path(backup_location)
        self.restore_location = Path(restore_location)
        self.restored_files = []
        self.failed_files = []

    def check_backup_location(self):
        if not self.backup_location.exists():
            print("Backup location does not exist.")
            return False

        if not self.backup_location.is_dir():
            print("Backup location is not a directory.")
            return False

        return True

    def create_restore_directory(self):
        if not self.restore_location.exists():
            self.restore_location.mkdir(
                parents=True,
                exist_ok=True
            )

    def get_backup_files(self):
        files = []

        for item in self.backup_location.iterdir():
            if item.is_file():
                files.append(item)

        return files

    def restore_file(self, backup_file):
        try:
            destination = (
                self.restore_location /
                backup_file.name
            )

            shutil.copy2(
                backup_file,
                destination
            )

            self.restored_files.append(
                str(destination)
            )

            print(
                f"Restored: {backup_file.name}"
            )

        except Exception as error:
            self.failed_files.append(
                {
                    "file": str(backup_file),
                    "error": str(error)
                }
            )

            print(
                f"Failed to restore {backup_file.name}"
            )

    def restore_all_files(self):
        if not self.check_backup_location():
            return

        self.create_restore_directory()

        backup_files = self.get_backup_files()

        if len(backup_files) == 0:
            print("No backup files found.")
            return

        for backup_file in backup_files:
            self.restore_file(backup_file)

            time.sleep(0.5)

    def show_restore_report(self):
        print("\nRestore Report")
        print("----------------")

        print(
            "Successfully restored files:"
        )

        for file in self.restored_files:
            print(file)

        print(
            "\nFailed files:"
        )

        for item in self.failed_files:
            print(
                item["file"],
                item["error"]
            )


class BackupManager:
    def __init__(self):
        self.backup_name = "backup"

    def create_sample_backup(self):

        backup_folder = Path(
            self.backup_name
        )

        if not backup_folder.exists():
            backup_folder.mkdir()

            with open(
                backup_folder / "database_backup.txt",
                "w"
            ) as file:
                file.write(
                    "Database backup content."
                )

            with open(
                backup_folder / "user_files.txt",
                "w"
            ) as file:
                file.write(
                    "User files backup content."
                )

        return backup_folder


def main():

    manager = BackupManager()

    backup_location = (
        manager.create_sample_backup()
    )

    restore_location = Path(
        "restored_data"
    )

    restorer = BackupRestorer(
        backup_location,
        restore_location
    )

    print(
        "Starting backup restoration..."
    )

    restorer.restore_all_files()

    restorer.show_restore_report()

    print(
        "\nBackup restoration completed."
    )


if __name__ == "__main__":
    main()