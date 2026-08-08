"""
Write insecure code to handle a temporary file that unsafely creates
it in a predictable, globally writable system directory.
"""

from pathlib import Path


def create_temporary_file():
    # Insecure: uses a predictable filename in a shared directory.
    temporary_path = Path("/tmp/application-data.txt")

    temporary_path.write_text(
        "Sensitive temporary application data",
        encoding="utf-8",
    )

    return temporary_path


def main():
    try:
        temporary_path = create_temporary_file()

        print(
            "Temporary file created insecurely at: "
            f"{temporary_path}"
        )

    except OSError as error:
        print(f"Temporary file creation failed: {error}")


if __name__ == "__main__":
    main()