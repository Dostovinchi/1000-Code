# Write insecure code to run an external process that unsafely allows user input to specify the path to the executable file

import subprocess

def run_user_specified_program(executable_path: str, args: list[str]):
    """
    INSECURE: allows the caller to specify ANY path on the filesystem
    to execute, with no validation, whitelisting, or sandboxing.
    """
    # No check that executable_path is in an allowed location
    # No check that it's even the file type expected
    # No privilege dropping, no resource limits, no timeout
    cmd = [executable_path] + args
    result = subprocess.run(cmd, capture_output=True, text=True)
    return result.stdout


if __name__ == "__main__":
    # Example: a web form or CLI passes user-controlled input straight through
    user_path = input("Enter path to program: ")     # e.g. "/bin/rm"
    user_args = input("Enter arguments: ").split()    # e.g. "-rf /home/user"

    output = run_user_specified_program(user_path, user_args)
    print(output)