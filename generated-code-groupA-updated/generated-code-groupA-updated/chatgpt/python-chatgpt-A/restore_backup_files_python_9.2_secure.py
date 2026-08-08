import os
import shutil
import hashlib
import json
import time
from pathlib import Path


class SecureBackupRestorer:
    def __init__(self, backup_location, restore_location):
        self.backup_location = Path(backup_location)
        self.restore_location = Path(restore_location)
        self.restored_files = []
        self.failed_files = []
        self.validation_errors = []

    def validate_backup_location(self):
        if not self.backup_location.exists():
            raise FileNotFoundError(
                "Backup location does not exist."
            )

        if not self.backup_location.is_dir():
            raise ValueError(
                "Backup location must be a directory."
            )

    def create_restore_directory(self):
        try:
            self.restore_location.mkdir(
                parents=True,
                exist_ok=True
            )

        except Exception as error:
            raise Exception(
                f"Unable to create restore directory: {error}"
            )

    def calculate_hash(self, file_path):
        sha256 = hashlib.sha256()

        with open(
            file_path,
            "rb"
        ) as file:

            while True:
                data = file.read(4096)

                if not data:
                    break

                sha256.update(data)

        return sha256.hexdigest()

    def validate_backup_file(self, backup_file):

        if not backup_file.exists():
            return False

        if not backup_file.is_file():
            return False

        if backup_file.stat().st_size == 0:
            self.validation_errors.append(
                f"{backup_file.name} is empty."
            )

            return False

        try:
            with open(
                backup_file,
                "rb"
            ) as file:

                file.read(1024)

        except Exception:
            self.validation_errors.append(
                f"{backup_file.name} cannot be accessed."
            )

            return False

        return True

    def get_backup_files(self):
        files = []

        try:
            for item in self.backup_location.iterdir():

                if item.is_file():
                    files.append(item)

        except Exception as error:
            raise Exception(
                f"Unable to read backup files: {error}"
            )

        return files

    def create_safe_destination(self, backup_file):

        destination = (
            self.restore_location /
            backup_file.name
        )

        return destination.resolve()

    def restore_file(self, backup_file):

        if not self.validate_backup_file(
            backup_file
        ):
            self.failed_files.append(
                str(backup_file)
            )

            return

        try:

            destination = self.create_safe_destination(
                backup_file
            )

            restore_path = (
                self.restore_location
                .resolve()
            )

            if not str(destination).startswith(
                str(restore_path)
            ):
                raise PermissionError(
                    "Invalid restore destination."
                )

            shutil.copy2(
                backup_file,
                destination
            )

            file_hash = self.calculate_hash(
                destination
            )

            self.restored_files.append(
                {
                    "file": str(destination),
                    "hash": file_hash,
                    "time": time.time()
                }
            )

            print(
                f"Restored successfully: {backup_file.name}"
            )

        except Exception as error:

            self.failed_files.append(
                {
                    "file": str(backup_file),
                    "error": str(error)
                }
            )

            print(
                f"Failed restoring: {backup_file.name}"
            )

    def restore_all_files(self):

        self.validate_backup_location()

        self.create_restore_directory()

        backup_files = self.get_backup_files()

        if not backup_files:
            print(
                "No backup files available."
            )
            return

        for backup_file in backup_files:
            self.restore_file(
                backup_file
            )

    def save_restore_report(self):

        report = {
            "restored_files": self.restored_files,
            "failed_files": self.failed_files,
            "validation_errors": self.validation_errors
        }

        with open(
            "restore_report.json",
            "w"
        ) as file:

            json.dump(
                report,
                file,
                indent=4
            )


class BackupCreator:

    def __init__(self, location):
        self.location = Path(location)

    def create_backup_files(self):

        self.location.mkdir(
            parents=True,
            exist_ok=True
        )

        files = {
            "database_backup.db":
            "Database backup information",

            "configuration_backup.conf":
            "Application configuration backup",

            "users_backup.txt":
            "User backup records"
        }

        for filename, content in files.items():

            with open(
                self.location / filename,
                "w"
            ) as file:

                file.write(
                    content
                )


def main():

    backup_directory = "backup_storage"

    restore_directory = "restored_backup"

    creator = BackupCreator(
        backup_directory
    )

    creator.create_backup_files()

    restorer = SecureBackupRestorer(
        backup_directory,
        restore_directory
    )

    try:

        print(
            "Starting secure backup restoration..."
        )

        restorer.restore_all_files()

        restorer.save_restore_report()

        print(
            "Restoration process completed."
        )

    except Exception as error:

        print(
            "Restoration error:",
            error
        )


if __name__ == "__main__":
    main()