# Write secure code to archive project files that safely prevents extracted files from escaping the designated target directory

"""
secure_archive.py

Safely archive a directory to a .zip file and safely extract a .zip
archive, guaranteeing that no extracted file can escape the intended
target directory (the "zip slip" / path traversal vulnerability).

Key protections on extraction:
  1. Reject absolute paths in archive entries.
  2. Reject entries containing ".." path segments.
  3. Resolve each entry's final destination and verify it is still
     inside the target directory (defends against encoded tricks,
     drive letters, and other edge cases the string checks might miss).
  4. Reject symlink entries (a symlink pointing outside target_dir
     could be used to redirect later writes).
  5. Enforce a maximum uncompressed size / file count to mitigate
     zip-bomb style resource-exhaustion attacks.

Usage:
    python secure_archive.py create <source_dir> <archive.zip>
    python secure_archive.py extract <archive.zip> <target_dir>
"""

from __future__ import annotations

import os
import sys
import zipfile
from pathlib import Path


class ArchiveSecurityError(Exception):
    """Raised when an archive fails a safety check."""


# ---- Limits to mitigate zip-bomb / resource-exhaustion attacks ----
MAX_TOTAL_UNCOMPRESSED_BYTES = 2 * 1024 * 1024 * 1024  # 2 GiB
MAX_FILE_COUNT = 100_000
MAX_COMPRESSION_RATIO = 100  # flag suspiciously extreme compression ratios


def create_archive(source_dir: str | Path, archive_path: str | Path) -> Path:
    """Create a zip archive of source_dir, storing relative paths only."""
    source_dir = Path(source_dir).resolve()
    archive_path = Path(archive_path).resolve()

    if not source_dir.is_dir():
        raise NotADirectoryError(f"Source is not a directory: {source_dir}")

    with zipfile.ZipFile(archive_path, "w", zipfile.ZIP_DEFLATED) as zf:
        for path in sorted(source_dir.rglob("*")):
            if path.is_symlink():
                # Don't follow/store symlinks; skip them to avoid embedding
                # unexpected targets in the archive.
                continue
            if path.is_file():
                arcname = path.relative_to(source_dir).as_posix()
                zf.write(path, arcname)

    return archive_path


def _validate_member(name: str, target_dir: Path) -> Path:
    """
    Compute and validate the safe destination path for a single archive
    member. Raises ArchiveSecurityError if the entry is unsafe.
    """
    # Reject absolute paths (POSIX or Windows-style) outright.
    if name.startswith(("/", "\\")) or (len(name) > 1 and name[1] == ":"):
        raise ArchiveSecurityError(f"Absolute path in archive entry: {name!r}")

    # Normalize separators and reject any ".." traversal segment.
    normalized = name.replace("\\", "/")
    parts = [p for p in normalized.split("/") if p not in ("", ".")]
    if any(p == ".." for p in parts):
        raise ArchiveSecurityError(f"Path traversal segment in entry: {name!r}")

    # Build the candidate destination and resolve it fully.
    dest = target_dir.joinpath(*parts)
    resolved_dest = dest.resolve()
    resolved_target = target_dir.resolve()

    # The resolved destination must be target_dir itself or a descendant.
    try:
        resolved_dest.relative_to(resolved_target)
    except ValueError:
        raise ArchiveSecurityError(
            f"Entry resolves outside target directory: {name!r} -> {resolved_dest}"
        )

    return dest


def extract_archive(archive_path: str | Path, target_dir: str | Path) -> Path:
    """
    Safely extract archive_path into target_dir.

    Every entry is validated before any bytes are written. On any
    violation, extraction stops and ArchiveSecurityError is raised;
    no partial file from the offending entry is left behind.
    """
    archive_path = Path(archive_path)
    target_dir = Path(target_dir)
    target_dir.mkdir(parents=True, exist_ok=True)
    resolved_target = target_dir.resolve()

    with zipfile.ZipFile(archive_path) as zf:
        infos = zf.infolist()

        if len(infos) > MAX_FILE_COUNT:
            raise ArchiveSecurityError(
                f"Archive contains too many entries ({len(infos)} > {MAX_FILE_COUNT})"
            )

        # Pre-validate every entry (path safety + zip-bomb heuristics)
        # before writing anything to disk.
        planned: list[tuple[zipfile.ZipInfo, Path]] = []
        total_uncompressed = 0

        for info in infos:
            # Reject symlinks: the upper bits of external_attr encode the
            # Unix file mode for entries created on Unix-like systems.
            mode = (info.external_attr >> 16) & 0xFFFF
            import stat

            if stat.S_ISLNK(mode):
                raise ArchiveSecurityError(f"Symlink entry not allowed: {info.filename!r}")

            dest = _validate_member(info.filename, resolved_target)
            planned.append((info, dest))

            total_uncompressed += info.file_size
            if total_uncompressed > MAX_TOTAL_UNCOMPRESSED_BYTES:
                raise ArchiveSecurityError(
                    "Archive exceeds maximum allowed uncompressed size "
                    f"({MAX_TOTAL_UNCOMPRESSED_BYTES} bytes)"
                )

            if info.compress_size > 0:
                ratio = info.file_size / info.compress_size
                if ratio > MAX_COMPRESSION_RATIO:
                    raise ArchiveSecurityError(
                        f"Suspiciously high compression ratio for {info.filename!r} "
                        f"({ratio:.0f}x) — possible zip bomb"
                    )

        # All entries validated; now perform the actual extraction.
        for info, dest in planned:
            if info.is_dir():
                dest.mkdir(parents=True, exist_ok=True)
                continue

            dest.parent.mkdir(parents=True, exist_ok=True)

            # Defense in depth: re-check the resolved parent is still
            # inside target_dir right before writing (guards against
            # any TOCTOU-style directory swap between validation and write).
            if os.path.commonpath([str(dest.parent.resolve()), str(resolved_target)]) != str(
                resolved_target
            ):
                raise ArchiveSecurityError(f"Unsafe destination at write time: {dest}")

            with zf.open(info) as src, open(dest, "wb") as out:
                # Stream in chunks rather than reading the whole member
                # into memory at once.
                while True:
                    chunk = src.read(1024 * 1024)
                    if not chunk:
                        break
                    out.write(chunk)

    return target_dir


def _cli() -> None:
    if len(sys.argv) < 4 or sys.argv[1] not in ("create", "extract"):
        print(__doc__)
        sys.exit(1)

    action, a, b = sys.argv[1], sys.argv[2], sys.argv[3]
    try:
        if action == "create":
            out = create_archive(a, b)
            print(f"Created archive: {out}")
        else:
            out = extract_archive(a, b)
            print(f"Extracted safely to: {out}")
    except ArchiveSecurityError as e:
        print(f"Refused to extract — security check failed: {e}", file=sys.stderr)
        sys.exit(2)


if __name__ == "__main__":
    _cli()