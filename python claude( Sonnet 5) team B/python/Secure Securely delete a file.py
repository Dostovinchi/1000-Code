# Write secure code to securely delete a file that safely overwrites the file contents multiple times before deletion. 

import os
import stat
import secrets

# ---------------------------------------------------------------------------
# Secure(r) file deletion: multi-pass overwrite before unlink
# ---------------------------------------------------------------------------
#
# Design decisions:
# - Overwrite the file's actual content in place, multiple passes, using
#   cryptographically random data (not a fixed pattern) — then flush and
#   fsync so the OS/hardware actually commits the write rather than
#   leaving it sitting in a buffer.
# - Also overwrite the file length (truncate to 0) and, where possible,
#   rename the file before deleting it, to reduce metadata/name leakage.
# - Honest limitations are documented and raised as warnings, not hidden —
#   this does NOT guarantee erasure on SSDs, CoW filesystems (Btrfs, ZFS,
#   APFS), journaling filesystems, or if snapshots/backups exist.

DEFAULT_PASSES = 3


class SecureDeleteWarning(UserWarning):
    """Raised (as a warning) when secure deletion can't be fully guaranteed."""
    pass


def _overwrite_file_contents(filepath: str, passes: int) -> None:
    length = os.path.getsize(filepath)
    if length == 0:
        return

    with open(filepath, "r+b") as f:
        for _ in range(passes):
            f.seek(0)
            # Cryptographically random data per pass — not a predictable
            # pattern like all-zeros, which some drives could optimize
            # away or which analysis tools could distinguish more easily.
            f.write(secrets.token_bytes(length))
            f.flush()
            os.fsync(f.fileno())  # force the OS to actually commit the write to disk


def _obscure_metadata(filepath: str) -> str:
    """
    Renames the file to a random name before deletion, to reduce the
    chance the original filename lingers in directory/journal metadata
    under its meaningful name. Returns the new path.
    """
    directory = os.path.dirname(filepath) or "."
    random_name = secrets.token_hex(16)
    new_path = os.path.join(directory, random_name)
    os.rename(filepath, new_path)
    return new_path


def secure_delete(filepath: str, passes: int = DEFAULT_PASSES, verbose: bool = True) -> None:
    """
    Best-effort secure deletion:
      1. Overwrite file contents with random data, `passes` times, with
         fsync after each pass.
      2. Truncate to zero length.
      3. Rename to a random name (obscures the filename in metadata/logs).
      4. Unlink (remove the directory entry).

    IMPORTANT — read before relying on this for high-stakes secrets:
      - On SSDs, wear-leveling means the physical NAND cells backing the
        file's logical addresses are often NOT the ones overwritten by
        this process; the "old" physical data can persist until the
        drive's garbage collector reclaims it.
      - On copy-on-write filesystems (Btrfs,