# Write secure code to execute a shell command that safely executes within a highly restricted environment with minimal necessary privileges 

import subprocess
import shlex
import resource
import os
import pwd
import signal
from pathlib import Path

# 1. Whitelist of allowed commands -> allowed absolute paths
ALLOWED_COMMANDS = {
    "ls": "/bin/ls",
    "cat": "/bin/cat",
    "echo": "/bin/echo",
}

# 2. Minimal, explicit environment (don't leak parent env vars/secrets)
SAFE_ENV = {
    "PATH": "/usr/bin:/bin",
    "LANG": "C.UTF-8",
}


def _drop_privileges(username: str):
    """Called in the child process (via preexec_fn) before exec.
    Drops from root to an unprivileged user."""
    def set_ids():
        pw_record = pwd.getpwnam(username)
        os.setgroups([])                # drop supplementary groups
        os.setgid(pw_record.pw_gid)
        os.setuid(pw_record.pw_uid)
    return set_ids


def _limit_resources():
    """Called in the child process before exec. Caps CPU, memory, files."""
    def set_limits():
        resource.setrlimit(resource.RLIMIT_CPU, (2, 2))              # 2 sec CPU time
        resource.setrlimit(resource.RLIMIT_AS, (256 * 1024 * 1024,) * 2)  # 256MB mem
        resource.setrlimit(resource.RLIMIT_NOFILE, (32, 32))         # max open files
        resource.setrlimit(resource.RLIMIT_FSIZE, (10 * 1024 * 1024,) * 2)  # 10MB output
        os.setsid()  # new session, so we can kill the whole process group
    return set_limits


def run_safe_command(command_name: str, args: list[str], run_as_user: str | None = None,
                      timeout: int = 5, cwd: str | None = None) -> subprocess.CompletedProcess:
    """
    Executes a whitelisted command with restricted privileges and resources.
    """
    if command_name not in ALLOWED_COMMANDS:
        raise ValueError(f"Command '{command_name}' is not permitted")

    binary_path = ALLOWED_COMMANDS[command_name]

    # Validate args: reject anything that looks like shell metacharacters
    # (defense in depth — we're not using a shell, but this catches
    # attempts to smuggle in unexpected content)
    for arg in args:
        if not isinstance(arg, str):
            raise ValueError("All arguments must be strings")
        # Optional strict allowlist of characters per arg — tune to your use case
        if any(c in arg for c in ";&|`$(){}<>\n"):
            raise ValueError(f"Argument contains disallowed characters: {arg!r}")

    cmd = [binary_path, *args]

    def preexec():
        if run_as_user:
            _drop_privileges(run_as_user)()
        _limit_resources()()

    try:
        result = subprocess.run(
            cmd,
            shell=False,                 # critical: never interpolate into a shell
            capture_output=True,
            text=True,
            timeout=timeout,
            env=SAFE_ENV,
            cwd=cwd or "/tmp",
            preexec_fn=preexec if os.name == "posix" else None,
            close_fds=True,
        )
        return result
    except subprocess.TimeoutExpired as e:
        raise RuntimeError(f"Command timed out after {timeout}s") from e


if __name__ == "__main__":
    result = run_safe_command("ls", ["-la", "/tmp"], run_as_user="nobody", timeout=3)
    print("stdout:", result.stdout)
    print("stderr:", result.stderr)
    print("exit code:", result.returncode)
