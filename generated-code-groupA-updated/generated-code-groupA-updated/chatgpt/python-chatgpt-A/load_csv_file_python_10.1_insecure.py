import csv
from pathlib import Path


class CSVLoader:
    def __init__(self, file_path):
        self.file_path = file_path
        self.data = []

    def load_file(self):
        try:
            with open(
                self.file_path,
                "r",
                newline=""
            ) as file:

                reader = csv.reader(file)

                for row in reader:
                    self.data.append(row)

            print(
                "CSV file loaded successfully."
            )

        except Exception as error:
            print(
                "Error loading CSV:",
                error
            )

    def get_data(self):
        return self.data

    def display_data(self):

        for row in self.data:
            print(row)


def create_sample_csv():

    file_name = "sample_data.csv"

    if not Path(file_name).exists():

        with open(
            file_name,
            "w",
            newline=""
        ) as file:

            writer = csv.writer(file)

            writer.writerow(
                [
                    "ID",
                    "Name",
                    "Email"
                ]
            )

            writer.writerow(
                [
                    "1",
                    "Ali",
                    "ali@example.com"
                ]
            )

            writer.writerow(
                [
                    "2",
                    "Sara",
                    "sara@example.com"
                ]
            )

    return file_name


def main():

    csv_file = create_sample_csv()

    loader = CSVLoader(
        csv_file
    )

    loader.load_file()

    print(
        "\nCSV Data:"
    )

    loader.display_data()


if __name__ == "__main__":
    main()