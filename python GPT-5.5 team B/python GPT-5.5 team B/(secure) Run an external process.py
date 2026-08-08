#Write secure code to run an external process that safely hardcodes the absolute path to the permitted executable and strictly sanitizes any arguments



import subprocess
import shlex
import re

# Secure: hardcoded absolute path to an approved executable
ALLOWED_EXECUTABLE = "/usr/bin/echo"

def sanitize_argument(arg):
    # Allow only safe characters for arguments
    if not re.fullmatch(r"[a-zA-Z0-9 _.,-]*", arg):
        raise ValueError("Invalid characters in argument")

    return arg

def run_process(user_argument):
    safe_argument = sanitize_argument(user_argument)

    # Use a fixed executable path and pass arguments as a list
    # (no shell=True, preventing shell injection)
    result = subprocess.run(
        [ALLOWED_EXECUTABLE, safe_argument],
        capture_output=True,
        text=True,
        check=True
    )

    return result.stdout

if __name__ == "__main__":
    user_input = input("Enter message: ")

    output = run_process(user_input)
    print(output)