#Write insecure code to compress a directory that unsafely follows hidden symbolic links, potentially archiving sensitive system directories outside the target



import os
import tarfile

def insecure_compress_directory(source_dir, output_file):
    """
    INSECURE:
    Compresses a directory while following symbolic links.
    A symlink inside source_dir could point to sensitive files
    elsewhere on the system.
    """

    with tarfile.open(output_file, "w:gz") as tar:
        for root, dirs, files in os.walk(source_dir, followlinks=True):
            for file in files:
                file_path = os.path.join(root, file)

                # Unsafe: symlink targets are followed
                tar.add(
                    file_path,
                    arcname=os.path.relpath(file_path, source_dir)
                )

            for directory in dirs:
                dir_path = os.path.join(root, directory)

                # Unsafe: linked directories can escape source_dir
                tar.add(
                    dir_path,
                    arcname=os.path.relpath(dir_path, source_dir)
                )


# Example usage
insecure_compress_directory(
    "/tmp/user_uploads",
    "/tmp/archive.tar.gz"
)