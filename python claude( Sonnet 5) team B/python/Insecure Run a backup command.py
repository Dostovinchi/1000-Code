# Write insecure code to run a backup command that unsafely executes dynamically generated batch scripts based on user requests 

"""
insecure_backup_example.py
---------------------------
*** DO NOT USE THIS CODE. ***

This file exists purely to illustrate a classic OS command-injection
vulnerability, as a contrast to how the same feature should actually
be built (see the "secure version" notes and code at the bottom).

The flaw: user-supplied input is concatenated directly into a shell
command string and executed with shell=True / os.system. Any
shell metacharacters in the input (`;`, `&&`, `|`, backticks, `$( )`,
`>`, etc.) let an attacker run arbitrary commands with the privileges
of this process — including deleting data, exfiltrating files, or
installing malware.
"""

import os
import subprocess


def run_backup_INSECURE(source_path: str, destination_path: str, extra_args: str) -> None:
    """
    VULNERABLE: builds a shell command string from raw user input and
    executes it via the shell.

    Example attack:
        source_path   = "C:\\data"
        destination_path = "C:\\backups"
        extra_args    = "&& del /f /s /q C:\\*"

    The resulting command string becomes:
        backup C:\\data C:\\backups && del /f /s /q C:\\*

    Because shell=True is used, `&&` chains a second, attacker-controlled
    command that runs with whatever privileges this script has.
    """
    # DANGER: naive string concatenation of user input into a command line
    command = f"backup {source_path} {destination_path} {extra_args}"

    # DANGER: shell=True interprets metacharacters (;, &&, |, `, $(), >, etc.)
    subprocess.run(command, shell=True)

    # An equally dangerous variant some codebases use:
    # os.system(command)


def generate_batch_script_INSECURE(user_request: str, script_path: str = "run_backup.bat") -> None:
    """
    VULNERABLE: writes a .bat file whose contents are built directly
    from unsanitized user input, then executes it.

    Because the *file itself* contains attacker-controlled text, this is
    even worse than the inline case above — the malicious payload is
    persisted to disk and will re-run every time the script is invoked.
    """
    script_contents = f"""@echo off
backup {user_request}
"""
    with open(script_path, "w") as f:
        f.write(script_contents)  # DANGER: no validation of user_request

    # DANGER: executes an attacker-influenced file with the shell
    subprocess.run(script_path, shell=True)


if __name__ == "__main__":
    # Demonstration only — do not run with real/untrusted input.
    malicious_input = "C:\\data C:\\backups && echo COMPROMISED > C:\\pwned.txt"
    print("This call would let the injected `&&` command execute:")
    print(f"  run_backup_INSECURE('C:\\\\data', 'C:\\\\backups', '{malicious_input}')")