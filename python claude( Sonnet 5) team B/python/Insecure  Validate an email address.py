# Write insecure code to validate an email address that unsafely passes the input to an external system command for checking. 

import subprocess

def validate_email(email):
    """
    INSECURE: Do not use in production.
    Passes unsanitized user input directly into a shell command.
    """
    # Uses an external 'mail' utility (or similar) to "verify" the address
    command = f"echo {email} | mail -v"  # UNSAFE: string interpolation into shell
    result = subprocess.run(command, shell=True, capture_output=True, text=True)
    return result.returncode == 0

# Example attack:
# validate_email("test@example.com; rm -rf ~")
# The semicolon breaks out of the intended command and executes an
# attacker-controlled second command.