# Write secure code to compress a directory that safely validates the canonical paths to ensure it only archives the intended target directory 

"""
secure_symlink_archive.py
---------------------------
A safe directory-compression routine.

Design principles:
  1. Symlinks are not followed transparently. Every path encountered
     during the walk — including hidden files/directories and
     symlinked ones — is resolved to its canonical filesystem location
     (`os.path.realpath`, which follows all symlinks and normalizes
     ".."/".") and verified to still live inside the target directory
     before it's ever added to the archive.
  2. Anything that resolves outside the target directory is rejected
     and reported, never silently included and never silently skipped
     without a trace — so an attacker planting a symlink gets a loud
     audit log entry instead of a quiet leak.
  3. The target directory itself is canonicalized once up front, so
     later comparisons are apples-to-apples (no mismatches from
     relative paths, trailing slashes, or the target dir itself being
     a symlink).
  4. Archive entries are stored with safe, traversal-free relative
     names, so extracting the resulting archive later can't escape
     its extraction directory either.
  5. Regular files, directories, and symlinks are all handled
     explicitly rather than relying on default `os.walk` behavior,
     so the containment check can't accidentally be bypassed.
"""

from __future__ import annotations

import logging
import os
import tarfile
from dataclasses import dataclass, field
from pathlib import Path

logger = logging.getLogger("secure_symlink_archive")
logger.setLevel(logging.INFO)
if not logger.handlers:
    _h = logging.StreamHandler()
    _h.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(message)s"))
    logger.addHandler(_h)


class ArchiveError(Exception):
    pass


@dataclass
class ArchiveResult:
    archive_path: Path
    included: list[str] = field(default_factory=list)
    rejected: list[tuple[str, str]] = field(default_factory=list)  # (path, reason)


def _is_within(path: Path, base: Path) -> bool:
    """True if the canonical `path` is base itself or nested inside it."""
    try:
        path.relative_to(base)
        return True
    except ValueError:
        return False


def compress_directory(source_dir: str | Path, archive_path: str | Path) -> ArchiveResult:
    """Compress `source_dir` into a tar.gz archive, refusing to include
    anything (via symlink or otherwise) that resolves outside of it.

    The target directory itself is canonicalized once. Every file and
    subdirectory encountered is then canonicalized again and checked
    for containment before being added — this defeats both regular
    symlinks (visible or hidden/dotfile-named) and more subtle tricks
    like a symlinked *parent* directory partway down the tree.
    """
    base = Path(source_dir).resolve(strict=True)  # canonical target root
    if not base.is_dir():
        raise ArchiveError(f"Source is not a directory: {base}")

    out_path = Path(archive_path)
    result = ArchiveResult(archive_path=out_path)

    with tarfile.open(out_path, "w:gz") as tar:
        # followlinks=False: os.walk will list symlinked directories as
        # entries but will NOT descend into them on its own. We handle
        # each entry explicitly below, so nothing is ever trusted
        # implicitly.
        for root, dirnames, filenames in os.walk(base, followlinks=False):
            root_path = Path(root)

            # Check each subdirectory before letting os.walk descend
            # into it. If a directory entry is a symlink, resolve it
            # and only allow the walk to continue if it stays inside
            # `base`; otherwise prune it from `dirnames` so os.walk
            # skips it entirely.
            safe_dirnames = []
            for d in dirnames:
                candidate = root_path / d
                try:
                    resolved = candidate.resolve(strict=True)
                except OSError as exc:
                    result.rejected.append((str(candidate), f"could not resolve: {exc}"))
                    continue

                if candidate.is_symlink() and not _is_within(resolved, base):
                    logger.warning(
                        "Refusing to follow symlinked directory outside target: %s -> %s",
                        candidate, resolved,
                    )
                    result.rejected.append((str(candidate), f"symlink escapes target dir (-> {resolved})"))
                    continue

                safe_dirnames.append(d)
            dirnames[:] = safe_dirnames  # prune in place so os.walk respects it

            for filename in filenames:
                full_path = root_path / filename
                try:
                    resolved = full_path.resolve(strict=True)
                except OSError as exc:
                    result.rejected.append((str(full_path), f"could not resolve: {exc}"))
                    continue

                if not _is_within(resolved, base):
                    logger.warning(
                        "Refusing to archive path outside target: %s -> %s",
                        full_path, resolved,
                    )
                    result.rejected.append((str(full_path), f"resolves outside target dir (-> {resolved})"))
                    continue

                if not resolved.is_file():
                    # Skip sockets, devices, broken links, etc.
                    result.rejected.append((str(full_path), "not a regular file"))
                    continue

                # Store with a safe, relative, traversal-free arcname.
                arcname = str(full_path.relative_to(base))
                tar.add(full_path, arcname=arcname)
                result.included.append(arcname)

    logger.info(
        "Archive complete: %s (%d files included, %d rejected)",
        out_path, len(result.included), len(result.rejected),
    )
    return result


if __name__ == "__main__":
    import shutil
    import tempfile

    with tempfile.TemporaryDirectory() as tmp:
        tmp_path = Path(tmp)
        target = tmp_path / "uploads"
        outside = tmp_path / "sensitive_system_dir"
        target.mkdir()
        outside.mkdir()

        (target / "notes.txt").write_text("normal file")
        (outside / "secret.txt").write_text("should never be archived")

        # Simulate an attacker (or misconfiguration) planting a hidden
        # symlink inside the target that points outside of it.
        (target / ".config").symlink_to(outside, target_is_directory=True)

        archive_path = tmp_path / "archive.tar.gz"
        outcome = compress_directory(target, archive_path)

        print(f"Included: {outcome.included}")
        print(f"Rejected: {outcome.rejected}")

        with tarfile.open(archive_path) as tar:
            print(f"Archive members: {tar.getnames()}")