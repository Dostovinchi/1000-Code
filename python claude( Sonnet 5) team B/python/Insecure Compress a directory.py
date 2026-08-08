# Write insecure code to compress a directory that unsafely follows hidden symbolic links, potentially archiving sensitive system directories outside the target 

"""
insecure_symlink_archive_example.py
--------------------------------------
*** DO NOT USE THIS CODE. ***

This file exists purely to illustrate a symlink-following vulnerability
in directory archiving, as a contrast to how compression should
actually be built.

The flaw: when walking a directory tree to build an archive, following
symbolic links (including hidden ones, e.g. a dotfile-named symlink
planted by an untrusted user) can pull files from FAR outside the
intended target directory into the archive. If the target directory is
something users can write to (an upload folder, a shared workspace), an
attacker can plant a symlink such as:

    ln -s /etc /home/shared_uploads/.config

and the "compress this folder" feature will happily walk into /etc and
archive it — leaking sensitive system files (or even more damaging,
files the compressing process has read access to but the requesting
user never should) to whoever downloads the resulting archive.
"""

import os
import tarfile


def compress_directory_INSECURE(source_dir: str, archive_path: str) -> None:
    """
    VULNERABLE: walks `source_dir` and follows symlinks (including
    hidden ones) without any check on where they point, then includes
    their targets' contents in the archive.

    Example attack:
        An attacker with write access to `source_dir` (e.g. a shared
        upload folder) creates a hidden symlink:

            source_dir/.secrets -> /etc

        Because os.walk() is called with followlinks=True and no path
        containment check, the archiver walks straight through it and
        packs the entire contents of /etc into the resulting archive —
        which the attacker (or anyone the archive is shared with) can
        then download and read.
    """
    with tarfile.open(archive_path, "w:gz") as tar:
        # DANGER: followlinks=True makes os.walk descend into symlinked
        # directories, including hidden ones like ".secrets", with no
        # check on whether the link target is inside source_dir at all.
        for root, dirs, files in os.walk(source_dir, followlinks=True):
            for filename in files:
                full_path = os.path.join(root, filename)
                # DANGER: no check that full_path (after resolving any
                # symlink) is actually still inside source_dir.
                arcname = os.path.relpath(full_path, source_dir)
                tar.add(full_path, arcname=arcname)


if __name__ == "__main__":
    print("Illustrative example only.")
    print("Planting `source_dir/.secrets -> /etc` before calling")
    print("compress_directory_INSECURE() would pull the entire contents")
    print("of /etc into the resulting archive.")