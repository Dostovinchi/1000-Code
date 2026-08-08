#Write insecure code to scan network hosts that unsafely passes raw user arguments directly into the scanning utility



import os

# Insecure: user input is directly inserted into a shell command
target = input("Enter host or network range to scan: ")

# Dangerous: raw input is executed by the shell
command = f"nmap {target}"

os.system(command)

print("Scan completed.")