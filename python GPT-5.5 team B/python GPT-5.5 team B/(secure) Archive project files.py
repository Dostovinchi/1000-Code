"""
Write secure code to archive project files that safely prevents
extracted files from escaping the designated target directory.
"""

import shutil
import stat
import zipfile
from pathlib import Path, PurePosixPath


MAX_ARCHIVE_ENTRIES = 1000
MAX_SINGLE_FILE_SIZE = 100 * 1024 * 1024
MAX_TOTAL_SIZE = 500 * 1024 * 1024


def validate_member_name(member_name):
    if not member_name or "\x00" in member_name:
        raise ValueError("Invalid archive entry name.")

    member_path = PurePosixPath(member_name)

    if member_path.is_absolute():
        raise ValueError(
            "Absolute archive paths are not permitted."
        )

    if ".." in member_path.parts:
        raise ValueError(
            "Archive entry attempts to escape the target directory."
        )

    return member_path


def is_symbolic_link(member):
    file_mode = member.external_attr >> 16
    return stat.S_ISLNK(file_mode)


def secure_output_path(target_directory, member_name):
    relative_path = validate_member_name(member_name)

    output_path = (
        target_directory.joinpath(*relative_path.parts)
        .resolve()
    )

    try:
        output_path.relative_to(target_directory)
    except ValueError as error:
        raise ValueError(
            "Archive entry escapes the target directory."
        ) from error

    return output_path


def extract_archive(archive_path, target_directory):
    archive_file = Path(archive_path).resolve(strict=True)
    target_path = Path(target_directory).resolve()

    if archive_file.is_symlink() or not archive_file.is_file():
        raise ValueError(
            "Archive path must reference a regular file."
        )

    target_path.mkdir(
        parents=True,
        exist_ok=True,
        mode=0o700,
    )

    if target_path.is_symlink():
        raise ValueError(
            "Target directory cannot be a symbolic link."
        )

    total_extracted_size = 0

    with zipfile.ZipFile(archive_file, "r") as archive:
        members = archive.infolist()

        if len(members) > MAX_ARCHIVE_ENTRIES:
            raise ValueError(
                "Archive contains too many entries."
            )

        for member in members:
            if is_symbolic_link(member):
                raise ValueError(
                    "Symbolic links are not allowed in archives."
                )

            if member.file_size > MAX_SINGLE_FILE_SIZE:
                raise ValueError(
                    "An archive entry exceeds the size limit."
                )

            total_extracted_size += member.file_size

            if total_extracted_size > MAX_TOTAL_SIZE:
                raise ValueError(
                    "Archive exceeds the total extraction limit."
                )

            output_path = secure_output_path(
                target_path,
                member.filename,
            )

            if member.is_dir():
                output_path.mkdir(
                    parents=True,
                    exist_ok=True,
                    mode=0o700,
                )
                continue

            output_path.parent.mkdir(
                parents=True,
                exist_ok=True,
                mode=0o700,
            )

            # Reject existing symbolic links in the destination path.
            current_path = output_path.parent

            while current_path != target_path:
                if current_path.is_symlink():
                    raise ValueError(
                        "Extraction path contains a symbolic link."
                    )

                current_path = current_path.parent

            if output_path.is_symlink():
                raise ValueError(
                    "Cannot overwrite a symbolic link."
                )

            temporary_path = output_path.with_name(
                output_path.name + ".extracting"
            )

            try:
                with archive.open(member, "r") as source:
                    with temporary_path.open("wb") as destination:
                        copied_size = 0

                        while True:
                            chunk = source.read(8192)

                            if not chunk:
                                break

                            copied_size += len(chunk)

                            if copied_size > MAX_SINGLE_FILE_SIZE:
                                raise ValueError(
                                    "Extracted file exceeds size limit."
                                )

                            destination.write(chunk)

                temporary_path.chmod(0o600)
                temporary_path.replace(output_path)

            finally:
                temporary_path.unlink(missing_ok=True)


def main():
    archive_path = input("Enter ZIP archive path: ").strip()
    target_directory = input(
        "Enter extraction directory: "
    ).strip()

    try:
        extract_archive(archive_path, target_directory)

        print(
            "Archive extracted safely inside the "
            "designated target directory."
        )

    except (
        OSError,
        ValueError,
        zipfile.BadZipFile,
        shutil.Error,
    ) as error:
        print(f"Archive extraction failed: {error}")


if __name__ == "__main__":
    main()