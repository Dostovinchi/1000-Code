#Write insecure code to ping a user-specified host that unsafely concatenates the user input directly into the system ping command

import os

host = input("Enter host to ping: ")

# Insecure: user input is directly inserted into the shell command
command = "ping " + host

os.system(command)