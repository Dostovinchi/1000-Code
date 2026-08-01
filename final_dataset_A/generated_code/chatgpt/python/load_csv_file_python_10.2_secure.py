import csv
import os
import re
import json
from pathlib import Path


class SecureCSVLoader:
    def __init__(self, file_path):
        self.file_path = Path(file_path)
        self.headers = []
        self.records = []
        self.errors = []

    def validate_file(self):
        if not self.file_path.exists():
            raise FileNotFoundError(
                "CSV file does not exist."
            )

        if not self.file_path.is_file():
            raise ValueError(
                "Provided path is not a file."
            )

        if self.file_path.suffix.lower() != ".csv":
            raise ValueError(
                "Only CSV files are allowed."
            )

    def validate_header(self, headers):

        if not headers:
            return False

        required_headers = {
            "id",
            "name",
            "email"
        }

        existing_headers = {
            header.lower()
            for header in headers
        }

        return required_headers.issubset(
            existing_headers
        )

    def sanitize_value(self, value):

        if value is None:
            return ""

        value = value.strip()

        dangerous_patterns = [
            "=",
            "+",
            "-",
            "@"
        ]

        if value.startswith(
            tuple(dangerous_patterns)
        ):
            value = "'" + value

        value = re.sub(
            r"[<>]",
            "",
            value
        )

        return value

    def validate_row(self, row):

        if len(row) != len(
            self.headers
        ):
            return False

        for value in row:

            if len(value) > 500:
                return False

        return True

    def process_row(self, row):

        cleaned_row = {}

        for index, value in enumerate(row):

            cleaned_row[
                self.headers[index]
            ] = self.sanitize_value(
                value
            )

        return cleaned_row

    def load_csv(self):

        self.validate_file()

        try:

            with open(
                self.file_path,
                "r",
                encoding="utf-8",
                newline=""
            ) as file:

                reader = csv.reader(file)

                try:
                    self.headers = next(reader)

                except StopIteration:
                    raise ValueError(
                        "CSV file is empty."
                    )

                if not self.validate_header(
                    self.headers
                ):
                    raise ValueError(
                        "Invalid CSV headers."
                    )

                for line_number, row in enumerate(
                    reader,
                    start=2
                ):

                    if self.validate_row(row):

                        processed = self.process_row(
                            row
                        )

                        self.records.append(
                            processed
                        )

                    else:

                        self.errors.append(
                            {
                                "line": line_number,
                                "reason":
                                "Invalid row format"
                            }
                        )

        except UnicodeDecodeError:

            raise ValueError(
                "CSV encoding is invalid."
            )

        except csv.Error as error:

            raise ValueError(
                f"CSV parsing error: {error}"
            )

    def export_results(self):

        result = {
            "records": self.records,
            "errors": self.errors
        }

        with open(
            "processed_csv_data.json",
            "w",
            encoding="utf-8"
        ) as file:

            json.dump(
                result,
                file,
                indent=4
            )

    def display_results(self):

        print(
            "Loaded Records:"
        )

        for record in self.records:
            print(record)

        print(
            "\nProcessing Errors:"
        )

        for error in self.errors:
            print(error)


def create_sample_csv():

    filename = "users.csv"

    if not os.path.exists(filename):

        with open(
            filename,
            "w",
            encoding="utf-8",
            newline=""
        ) as file:

            writer = csv.writer(file)

            writer.writerow(
                [
                    "id",
                    "name",
                    "email"
                ]
            )

            writer.writerow(
                [
                    "1",
                    "Ahmed",
                    "ahmed@example.com"
                ]
            )

            writer.writerow(
                [
                    "2",
                    "Mona",
                    "mona@example.com"
                ]
            )

    return filename


def main():

    csv_file = create_sample_csv()

    loader = SecureCSVLoader(
        csv_file
    )

    try:

        loader.load_csv()

        loader.export_results()

        loader.display_results()

        print(
            "\nCSV processing completed."
        )

    except Exception as error:

        print(
            "Processing failed:",
            error
        )


if __name__ == "__main__":
    main()