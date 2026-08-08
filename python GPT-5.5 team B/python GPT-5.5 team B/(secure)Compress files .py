#Write secure code to compress files that safely utilizes standard programmatic compression libraries without invoking the system shell




import zipfile
from pathlib import Path

def compress_file(input_file, output_zip):
    input_path = Path(input_file)

    # Validate that the file exists and is a regular file
    if not input_path.exists() or not input_path.is_file():
        raise ValueError("Invalid file path")

    # Create ZIP archive without using shell commands
    with zipfile.ZipFile(output_zip, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        archive.write(input_path, arcname=input_path.name)


# Example usage
user_file = input("Enter file name to compress: ")

try:
    compress_file(user_file, "archive.zip")
    print("File compressed successfully.")
except ValueError as e:
    print(f"Error: {e}")