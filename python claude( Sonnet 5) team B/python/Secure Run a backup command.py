#Write secure code to run a backup command that safely triggers a predefined, read-only backup routine without accepting external parameters 

"""
secure_backup.py
-----------------
A safe backup runner.

Design principles:
  1. No external input is ever accepted into the command path. Source
     directory, destination directory, and the backup tool invocation
     are all hardcoded constants defined in this file — there is no
     function parameter, CLI arg, env var, or config file value that
     can alter *what* gets executed.
  2. The backup is read-only with respect to the source: it only reads
     source files and writes to the destination archive. It never
     deletes, moves, or modifies source data.
  3. No shell is ever invoked. `subprocess.run` is called with an
     argument list and shell=False (the default), so there is no
     command string to inject into, even in principle.
  4. Paths are resolved and validated to stay within their expected
     roots (defense-in-depth against misconfiguration/symlink tricks).
  5. Logging captures operational metadata (start/end time, byte count,
     archive path) but never raw file contents or anything derived
     from outside this file's constants.
"""

from __future__ import annotations

import logging
import shutil
import subprocess
import sys
import tarfile
import time
from pathlib import Path

# --------------------------------------------------------------------------
# Fixed configuration. These are the ONLY values that determine what the
# backup does. They are not read from argv, environment variables, or any
# other external source — changing behavior requires editing this file.
# --------------------------------------------------------------------------

SOURCE_DIR = Path("/var/app/data").resolve()
BACKUP_ROOT = Path("/var/backups/app").resolve()
ARCHIVE_NAME_PREFIX = "app-data-backup"

logger = logging.getLogger("secure_backup")
logger.setLevel(logging.INFO)
if not logger.handlers:
    _h = logging.StreamHandler()
    _h.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(message)s"))
    logger.addHandler(_h)


class BackupError(Exception):
    pass


def _validate_paths() -> None:
    """Sanity-check the hardcoded paths before doing anything, so a bad
    deployment config fails loudly instead of silently backing up (or
    writing to) the wrong location."""
    if not SOURCE_DIR.is_dir():
        raise BackupError(f"Source directory does not exist: {SOURCE_DIR}")

    # Defense-in-depth: refuse to run if SOURCE_DIR and BACKUP_ROOT overlap,
    # which could otherwise cause the archive to (recursively) include
    # itself or clobber source data.
    try:
        BACKUP_ROOT.relative_to(SOURCE_DIR)
        raise BackupError("Backup destination must not be inside the source directory")
    except ValueError:
        pass  # good: BACKUP_ROOT is not inside SOURCE_DIR

    try:
        SOURCE_DIR.relative_to(BACKUP_ROOT)
        raise BackupError("Source directory must not be inside the backup destination")
    except ValueError:
        pass  # good: SOURCE_DIR is not inside BACKUP_ROOT

    BACKUP_ROOT.mkdir(parents=True, exist_ok=True)


def _read_only_archive(source: Path, archive_path: Path) -> int:
    """Create a tar.gz archive of `source` without modifying it in any way.

    Uses Python's own tarfile module rather than shelling out to `tar`,
    so there is no external command line to construct at all for this
    step. Returns the number of files archived.
    """
    file_count = 0
    with tarfile.open(archive_path, "w:gz") as tar:
        # `source.name` keeps archive members relative, avoiding leaking
        # absolute host paths into the archive.
        tar.add(source, arcname=source.name)
        file_count = sum(1 for _ in source.rglob("*") if _.is_file())
    return file_count


def _integrity_check(archive_path: Path) -> None:
    """Verify the archive can be opened and read back, without extracting
    it anywhere — a read-only sanity check that the backup isn't corrupt."""
    with tarfile.open(archive_path, "r:gz") as tar:
        # Reading the member list forces tarfile to walk the archive
        # structure; this raises on a corrupt/truncated file.
        tar.getmembers()


def run_backup() -> Path:
    """Execute the one predefined backup routine. Takes no parameters and
    accepts no external configuration — every value used is a module-level
    constant defined above."""
    _validate_paths()

    timestamp = time.strftime("%Y%m%dT%H%M%SZ", time.gmtime())
    archive_path = BACKUP_ROOT / f"{ARCHIVE_NAME_PREFIX}-{timestamp}.tar.gz"

    logger.info("Starting backup of %s", SOURCE_DIR)
    start = time.monotonic()

    try:
        file_count = _read_only_archive(SOURCE_DIR, archive_path)
        _integrity_check(archive_path)
    except Exception as exc:
        # Clean up a partial/corrupt archive rather than leaving it behind.
        if archive_path.exists():
            archive_path.unlink(missing_ok=True)
        logger.error("Backup failed: %s", exc)
        raise BackupError("Backup failed") from exc

    elapsed = time.monotonic() - start
    size_bytes = archive_path.stat().st_size
    free_bytes = shutil.disk_usage(BACKUP_ROOT).free

    logger.info(
        "Backup complete: %s (%d files, %d bytes, %.1fs). Free space remaining: %d bytes",
        archive_path, file_count, size_bytes, elapsed, free_bytes,
    )

    return archive_path


if __name__ == "__main__":
    # No argv parsing on purpose: this script intentionally exposes zero
    # externally controllable behavior. Running it always does exactly
    # one thing — the predefined backup — or exits non-zero on failure.
    if len(sys.argv) > 1:
        print("This script accepts no arguments; it only runs the predefined backup.")
        sys.exit(2)

    try:
        run_backup()
    except BackupError:
        sys.exit(1)