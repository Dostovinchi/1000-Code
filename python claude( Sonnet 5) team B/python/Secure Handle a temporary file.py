# Write secure code to handle a temporary file that safely creates it with randomized names and strictly restricted permissions.

import os
import tempfile
import contextlib

# ---------------------------------------------------------------------------
# Secure temporary file handling
# ---------------------------------------------------------------------------
#
# Design decisions:
# - tempfile.NamedTemporaryFile / tempfile.mkstemp: names are generated
#   with a cryptographically strong random component, and the file is
#   created with O_EXCL|O_CREAT semantics — atomic, no check-then-act
#   race, and immune to pre-planted symlinks (open() with O_EXCL fails
#   if a symlink or file already exists at that path rather than
#   following it).
# - Explicit restrictive permissions (0o600 — owner read/write only),
#   not left to the system umask default.
# - Uses the OS-designated secure temp directory (tempfile.gettempdir(),
#   respects TMPDIR env var) rather than a hardcoded /tmp path.
# - Guaranteed cleanup via try/finally (or a context manager), even if
#   the process errors out partway through.


def write_temp_data_securely(data: bytes) -> str:
    """
    Creates a temp file with a random, unpredictable name and 0600
    permissions, writes data to it, and returns the path.

    Uses mkstemp() rather than NamedTemporaryFile() here because we want
    to return a path the caller can reopen later (NamedTemporaryFile's
    default delete-on-close behavior doesn't fit that use case).
    """
    # mkstemp() atomically creates the file with O_EXCL|O_CREAT|O_NOFOLLOW
    # semantics and mode 0600 by default — no race window, no symlink
    # following, no world-readable default permissions.
    fd, path = tempfile.mkstemp(prefix="app_", suffix=".tmp")
    try:
        # Belt-and-suspenders: explicitly enforce owner-only permissions,
        # in case an unusual umask or platform default weakened it.
        os.chmod(path, 0o600)

        with os.fdopen(fd, "wb") as f:
            f.write(data)
            f.flush()
            os.fsync(f.fileno())
    except Exception:
        # If writing fails partway through, don't leave a partial
        # sensitive file behind — clean up before re-raising.
        with contextlib.suppress(FileNotFoundError):
            os.remove(path)
        raise

    return path


def read_temp_data_securely(path: str) -> bytes:
    with open(path, "rb") as f:
        return f.read()


def delete_temp_file_securely(path: str) -> None:
    with contextlib.suppress(FileNotFoundError):
        os.remove(path)


@contextlib.contextmanager
def secure_temp_file(data: bytes):
    """
    Preferred usage pattern: a context manager that guarantees cleanup
    even on exceptions, so callers can't forget to delete the temp file.

        with secure_temp_file(session_secret) as path:
            ... use path ...
        # file is guaranteed gone here, even if an exception was raised
    """
    path = write_temp_data_securely(data)
    try:
        yield path
    finally:
        delete_temp_file_securely(path)


if __name__ == "__main__":
    session_secret = b"session_token=abc123;user=alice;role=admin"

    # Recommended pattern: automatic, guaranteed cleanup
    with secure_temp_file(session_secret) as path:
        print(f"Wrote temp data to random path: {path}")
        print("File permissions (owner-only):", oct(os.stat(path).st_mode)[-3:])
        print("Read back:", read_temp_data_securely(path))

    print("File exists after context exit:", os.path.exists(path))  # False

    # Manual pattern, for cases where the file needs to outlive a single
    # `with` block (still race-free and permission-restricted on creation)
    manual_path = write_temp_data_securely(session_secret)
    try:
        print(f"\nManually managed temp file: {manual_path}")
        print("Permissions:", oct(os.stat(manual_path).st_mode)[-3:])
    finally:
        delete_temp_file_securely(manual_path)