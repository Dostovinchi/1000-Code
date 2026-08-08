# Write secure code to run an external process that safely hardcodes the absolute path to the permitted executable and strictly sanitizes any arguments

import subprocess
import shlex
import re
from pathlib import Path

# 1. Hardcode the absolute path to the permitted executable.
#    Never build this path from user input, environment variables, or PATH lookup.
ALLOWED_EXECUTABLE = Path("/usr/bin/convert")  # example: ImageMagick

# 2. Verify the executable actually exists at that exact path and is a file
#    (not a symlink to something unexpected) before ever using it.
def _validate_executable(path: Path) -> Path:
    resolved = path.resolve(strict=True)
    if resolved != path:
        raise RuntimeError(f"Executable path resolves elsewhere: {resolved}")
    if not resolved.is_file():
        raise RuntimeError(f"Not a regular file: {resolved}")
    return resolved

EXECUTABLE = _validate_executable(ALLOWED_EXECUTABLE)

# 3. Strictly whitelist argument content. Reject anything that doesn't
#    match an explicit allowed pattern instead of trying to "escape" it.
_SAFE_ARG_RE = re.compile(r"^[A-Za-z0-9._\-/]+$")

def sanitize_arg(arg: str, max_len: int = 256) -> str:
    if not isinstance(arg, str):
        raise TypeError("Argument must be a string")
    if not (1 <= len(arg) <= max_len):
        raise ValueError("Argument length out of bounds")
    if not _SAFE_ARG_RE.fullmatch(arg):
        raise ValueError(f"Argument contains disallowed characters: {arg!r}")
    # Reject anything that looks like a flag injection (e.g. "-rf", "--help")
    # unless flags are explicitly expected and whitelisted separately.
    if arg.startswith("-"):
        raise ValueError(f"Argument must not start with '-': {arg!r}")
    return arg

def sanitize_path_arg(arg: str, base_dir: Path) -> str:
    """Use this instead of sanitize_arg() when the argument is a filesystem path.
    Ensures the resolved path stays inside an allowed base directory
    (prevents path traversal like ../../etc/passwd)."""
    candidate = (base_dir / arg).resolve()
    base_resolved = base_dir.resolve()
    if not str(candidate).startswith(str(base_resolved) + "/"):
        raise ValueError(f"Path escapes allowed directory: {arg!r}")
    if not candidate.exists():
        raise ValueError(f"Path does not exist: {candidate}")
    return str(candidate)

# 4. Run the process safely.
def run_external_process(user_supplied_args: list[str], base_dir: Path) -> subprocess.CompletedProcess:
    safe_args = [sanitize_path_arg(a, base_dir) for a in user_supplied_args]

    # argv list form — never shell=True, never string concatenation.
    argv = [str(EXECUTABLE), *safe_args]

    result = subprocess.run(
        argv,
        shell=False,           # critical: avoids shell metacharacter interpretation
        capture_output=True,
        text=True,
        timeout=10,             # avoid hangs / DoS
        check=False,
        env={"PATH": "/usr/bin:/bin"},  # minimal, controlled environment
    )
    return result

# --- Example usage ---
if __name__ == "__main__":
    base = Path("/var/app/uploads")
    try:
        proc = run_external_process(["image1.png"], base)
        print(proc.stdout)
    except (ValueError, RuntimeError, TypeError) as e:
        print(f"Rejected: {e}")