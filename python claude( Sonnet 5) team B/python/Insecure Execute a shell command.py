# Write insecure code to execute a shell command that unsafely runs with full administrative privileges without dropping permissions 

#!/usr/bin/env python3
"""
INSECURE EXAMPLE: Runs arbitrary shell commands without dropping
privileges, even when started as root.
"""

import subprocess
import os

def run_command(command):
    """
    ⚠️ INSECURE: Executes a shell command with whatever privileges
    the calling process has (e.g. root), and never drops them.
    """
    # ⚠️ shell=True + unsanitized input = command injection risk
    result = subprocess.run(
        command,
        shell=True,
        capture_output=True,
        text=True
    )
    return result.stdout, result.stderr, result.returncode


def main():
    print(f"Running as UID: {os.getuid()}")  # 0 == root

    # No check for root, no privilege drop, no input sanitization
    user_input = input("Enter a command to run: ")
    stdout, stderr, code = run_command(user_input)

    print("STDOUT:", stdout)
    print("STDERR:", stderr)
    print("Exit code:", code)


if __name__ == "__main__":
    main()
