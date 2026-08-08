# Write secure code to clean log files that safely performs scheduled cleanup tasks strictly on pre-configured, protected server directories 

import os
import re
import time
import logging
from dataclasses import dataclass
from pathlib import Path
from typing import List

# --------------------------------------------------------------------------
# Configuration is defined server-side only. No client input is ever
# accepted here — this is the core of what makes the design safe.
# --------------------------------------------------------------------------

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
)
logger = logging.getLogger("log_cleanup")


@dataclass(frozen=True)
class CleanupRule:
    directory: Path          # must be an allow-listed, pre-configured directory
    pattern: re.Pattern      # only filenames matching this pattern are eligible
    max_age_days: int        # files older than this are deleted
    recursive: bool = False  # whether to descend into subdirectories


# Explicit allow-list of directories that may ever be touched.
# Nothing outside this list can be cleaned, regardless of any other input.
ALLOWED_LOG_DIRS = {
    Path("/var/app/logs").resolve(),
    Path("/var/app/logs/archive").resolve(),
}

CLEANUP_RULES: List[CleanupRule] = [
    CleanupRule(
        directory=Path("/var/app/logs").resolve(),
        pattern=re.compile(r"^app-\d{4}-\d{2}-\d{2}\.log$"),
        max_age_days=14,
        recursive=False,
    ),
    CleanupRule(
        directory=Path("/var/app/logs/archive").resolve(),
        pattern=re.compile(r"^app-\d{4}-\d{2}-\d{2}\.log\.gz$"),
        max_age_days=90,
        recursive=False,
    ),
]


class UnsafeDirectoryError(Exception):
    """Raised if a rule ever points outside the allow-listed directories."""


def _validate_directory(directory: Path) -> None:
    resolved = directory.resolve()
    if resolved not in ALLOWED_LOG_DIRS:
        raise UnsafeDirectoryError(
            f"Refusing to operate on non-allow-listed directory: {resolved}"
        )
    if not resolved.is_dir():
        raise UnsafeDirectoryError(f"Not a directory: {resolved}")


def _is_safe_file(base_dir: Path, file_path: Path, pattern: re.Pattern) -> bool:
    """
    Defense in depth: even though we generate the file list ourselves via
    directory iteration (no client input), we re-validate every candidate
    before deleting it.
    """
    try:
        resolved = file_path.resolve()
    except OSError:
        return False

    # Must remain inside the base directory (guards against symlink escapes)
    if base_dir not in resolved.parents and resolved.parent != base_dir:
        return False

    # Must be a real file, not a symlink pointing elsewhere
    if file_path.is_symlink():
        return False

    if not resolved.is_file():
        return False

    # Must match the expected naming pattern for this rule
    if not pattern.match(resolved.name):
        return False

    return True


def _iter_candidate_files(rule: CleanupRule):
    if rule.recursive:
        yield from rule.directory.rglob("*")
    else:
        yield from rule.directory.iterdir()


def clean_directory(rule: CleanupRule, dry_run: bool = False) -> int:
    """
    Applies a single cleanup rule. Returns the number of files deleted.
    """
    _validate_directory(rule.directory)

    cutoff = time.time() - (rule.max_age_days * 86400)
    deleted_count = 0

    for candidate in _iter_candidate_files(rule):
        if not _is_safe_file(rule.directory, candidate, rule.pattern):
            continue

        try:
            mtime = candidate.stat().st_mtime
        except OSError as e:
            logger.warning("Could not stat %s: %s", candidate, e)
            continue

        if mtime >= cutoff:
            continue  # not old enough yet

        if dry_run:
            logger.info("[dry-run] Would delete: %s", candidate)
            deleted_count += 1
            continue

        try:
            candidate.unlink()
            logger.info("Deleted: %s", candidate)
            deleted_count += 1
        except OSError as e:
            logger.error("Failed to delete %s: %s", candidate, e)

    return deleted_count


def run_scheduled_cleanup(dry_run: bool = False) -> None:
    logger.info("Starting scheduled log cleanup (dry_run=%s)", dry_run)
    total_deleted = 0

    for rule in CLEANUP_RULES:
        try:
            total_deleted += clean_directory(rule, dry_run=dry_run)
        except UnsafeDirectoryError as e:
            # Fail loudly rather than silently skipping — a misconfigured
            # rule should be treated as a bug to fix, not ignored.
            logger.critical("Cleanup rule misconfigured: %s", e)
            raise

    logger.info("Cleanup complete. Total files deleted: %d", total_deleted)


if __name__ == "__main__":
    # Run with dry_run=True first when deploying new rules to verify behavior.
    run_scheduled_cleanup(dry_run=False)