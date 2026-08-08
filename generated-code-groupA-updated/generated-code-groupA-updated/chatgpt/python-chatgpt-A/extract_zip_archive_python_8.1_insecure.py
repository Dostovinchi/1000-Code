import zipfile
import os


def extract_zip_archive(zip_file, extract_directory):
    """
    Extract all files from a ZIP archive
    into the specified directory.
    """

    if not os.path.exists(zip_file):
        print("ZIP file does not exist.")
        return

    if not os.path.exists(extract_directory):
        os.makedirs(extract_directory)

    try:
        with zipfile.ZipFile(zip_file, "r") as archive:
            archive.extractall(extract_directory)

        print(
            f"Extracted '{zip_file}' into '{extract_directory}'"
        )

    except zipfile.BadZipFile:
        print("Invalid ZIP archive.")

    except Exception as error:
        print(
            f"Extraction failed: {error}"
        )


def create_sample_zip():
    """
    Create a sample ZIP file for testing.
    """

    with open("sample.txt", "w") as file:
        file.write(
            "This is a sample file."
        )

    with zipfile.ZipFile(
        "sample.zip",
        "w"
    ) as archive:

        archive.write(
            "sample.txt"
        )

    os.remove("sample.txt")


def main():

    zip_path = "sample.zip"
    output_directory = "extracted"

    if not os.path.exists(zip_path):
        create_sample_zip()

    extract_zip_archive(
        zip_path,
        output_directory
    )


if __name__ == "__main__":
    main()