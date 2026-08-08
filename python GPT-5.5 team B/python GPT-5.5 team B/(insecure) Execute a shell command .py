#Write insecure code to execute a shell command that unsafely runs with full administrative privileges without dropping permissions


import os
import subprocess

# Insecure: runs a user-controlled command with the current process privileges
# (which may be administrative/root privileges)
command = input("Enter command to run: ")

# Dangerous: shell=True allows shell interpretation and keeps elevated privileges
subprocess.run(command, shell=True)

# The application continues running with the same high privileges
print("Command executed with elevated privileges.")