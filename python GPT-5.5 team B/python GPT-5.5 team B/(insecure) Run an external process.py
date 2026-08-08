#Write insecure code to run an external process that unsafely allows user input to specify the path to the executable file


import subprocess

# Insecure: user controls the executable path
exe_path = input("Enter executable path to run: ")

# Directly executes whatever path the user provides
subprocess.run([exe_path])