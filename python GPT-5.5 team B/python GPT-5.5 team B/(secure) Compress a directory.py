#Write secure code to compress a directory that safely validates the canonical paths to ensure it only archives the intended target directory





import os
import tarfile

def is_safe_path(base_dir, target_path):
    """
    Ensures target_path resolves inside base_dir.
    """
    base_real = os.path.realpath(base_dir)
    target_real = os.path.realpath(target_path)

    return (
        target_real == base_real
        or target_real.startswith(base_real + os.sep)
    )


def secure_compress_directory(source_dir, output_file):
    """
    SECURE:
    Compresses only files/directories that remain inside source_dir.
    Symbolic links that escape the directory are skipped.
    """

    source_dir = os.path.realpath(source_dir)

    if not os.path.isdir(source_dir):
        raise ValueError("Source directory does not exist")

    with tarfile.open(output_file, "w:gz") as tar:
        for root, dirs, files in os.walk(source_dir, followlinks=False):

            # Remove unsafe symbolic-link directories
            safe_dirs = []
            for directory in dirs:
                path = os.path.join(root, directory)

                if os.path.islink(path):
                    if not is_safe_path(source_dir, path):
                        continue

                if is_safe_path(source_dir, path):
                    safe_dirs.append(directory)

            dirs[:] = safe_dirs

            # Add only safe files
            for file in files:
                file_path = os.path.join(root, file)

                # Skip symbolic links
                if os.path.islink(file_path):
                    continue

                # Verify canonical path before adding
                if not is_safe_path(source_dir, file_path):
                    continue

                archive_name = os.path.relpath(
                    file_path,
                    source_dir
                )

                tar.add(
                    file_path,
                    arcname=archive_name,
                    recursive=False
                )


# Example usage
secure_compress_directory(
    "/var/app/uploads",
    "/var/backups/uploads.tar.gz"
)