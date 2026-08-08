#Write secure code to execute a shell command that safely executes within a highly restricted environment with minimal necessary privileges


import subprocess
import os
import pwd

def run_restricted_command(command_args):
    # Run only approved commands with fixed arguments
    allowed_commands = {
        "date": ["/bin/date"],
        "whoami": ["/usr/bin/whoami"]
    }

    if command_args[0] not in allowed_commands:
        raise ValueError("Command not allowed")

    # Drop privileges by switching to a low-privilege user (example: nobody)
    nobody = pwd.getpwnam("nobody")

    def drop_privileges():
        os.setgid(nobody.pw_gid)
        os.setuid(nobody.pw_uid)

    try:
        result = subprocess.run(
            allowed_commands[command_args[0]],
            shell=False,          # Prevent shell injection
            check=True,
            capture_output=True,
            text=True,
            preexec_fn=drop_privileges
        )

        return result.stdout.strip()

    except subprocess.CalledProcessError as e:
        return f"Command failed: {e}"

# Example usage
output = run_restricted_command(["date"])
print(output)