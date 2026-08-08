import os
import zipfile
import shutil
import hashlib
from pathlib import Path


class SecureZipExtractor:
    def __init__(self, zip_path, extract_directory):
        self.zip_path = Path(zip_path)
        self.extract_directory = Path(extract_directory)
        self.extracted_files = []

    def validate_zip_file(self):
        if not self.zip_path.exists():
            raise FileNotFoundError("ZIP file does not exist.")

        if not zipfile.is_zipfile(self.zip_path):
            raise ValueError("The provided file is not a valid ZIP archive.")

        return True

    def create_destination_directory(self):
        try:
            self.extract_directory.mkdir(
                parents=True,
                exist_ok=True
            )
        except Exception as error:
            raise Exception(
                f"Unable to create extraction directory: {error}"
            )

    def sanitize_filename(self, filename):
        filename = filename.replace("\\", "/")

        while filename.startswith("/"):
            filename = filename[1:]

        return filename

    def validate_archive_entry(self, entry_name):
        safe_name = self.sanitize_filename(entry_name)

        destination = (
            self.extract_directory / safe_name
        ).resolve()

        base_directory = (
            self.extract_directory.resolve()
        )

        if not str(destination).startswith(
            str(base_directory)
        ):
            raise SecurityError(
                "Unsafe archive entry detected."
            )

        return safe_name

    def calculate_file_hash(self, file_path):
        sha256 = hashlib.sha256()

        with open(file_path, "rb") as file:
            while True:
                data = file.read(4096)

                if not data:
                    break

                sha256.update(data)

        return sha256.hexdigest()

    def extract_files(self):
        self.validate_zip_file()
        self.create_destination_directory()

        try:
            with zipfile.ZipFile(
                self.zip_path,
                "r"
            ) as archive:

                entries = archive.infolist()

                for entry in entries:

                    if entry.is_dir():
                        continue

                    safe_name = self.validate_archive_entry(
                        entry.filename
                    )

                    output_path = (
                        self.extract_directory /
                        safe_name
                    )

                    output_path.parent.mkdir(
                        parents=True,
                        exist_ok=True
                    )

                    with archive.open(entry) as source:
                        with open(
                            output_path,
                            "wb"
                        ) as destination:

                            shutil.copyfileobj(
                                source,
                                destination
                            )

                    file_hash = self.calculate_file_hash(
                        output_path
                    )

                    self.extracted_files.append(
                        {
                            "file": str(output_path),
                            "hash": file_hash
                        }
                    )

        except zipfile.BadZipFile:
            raise ValueError(
                "The ZIP archive is corrupted."
            )

        except Exception as error:
            raise Exception(
                f"Extraction failed: {error}"
            )

    def show_extracted_files(self):
        print(
            "\nExtracted Files:"
        )

        for item in self.extracted_files:
            print(
                "File:",
                item["file"]
            )

            print(
                "SHA256:",
                item["hash"]
            )

            print(
                "----------------"
            )


class SecurityError(Exception):
    pass


def create_test_archive():
    archive_name = "sample_archive.zip"

    if not os.path.exists(archive_name):

        with zipfile.ZipFile(
            archive_name,
            "w"
        ) as archive:

            with open(
                "example.txt",
                "w"
            ) as file:
                file.write(
                    "Example file content."
                )

            archive.write(
                "example.txt"
            )

    return archive_name


def main():

    zip_file = create_test_archive()

    output_directory = "extracted_files"

    extractor = SecureZipExtractor(
        zip_file,
        output_directory
    )

    try:
        extractor.extract_files()

        extractor.show_extracted_files()

        print(
            "ZIP extraction completed successfully."
        )

    except Exception as error:
        print(
            "Error:",
            error
        )


if __name__ == "__main__":
    main()