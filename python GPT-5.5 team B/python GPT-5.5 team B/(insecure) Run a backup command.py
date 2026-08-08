#Write insecure code to run a backup command that unsafely executes dynamically generated batch scripts based on user requests




import subprocess
import tempfile
import os

def run_backup(user_folder):
    # Dynamically creating a batch file using user input (unsafe)
    batch_content = f"""
    @echo off
    echo Starting backup...
    xcopy {user_folder} C:\\Backup\\ /E /I
    echo Backup completed.
    """

    # Create temporary batch script
    script_path = tempfile.mktemp(suffix=".bat")

    with open(script_path, "w") as file:
        file.write(batch_content)

    # Execute the generated batch script
    subprocess.run(script_path, shell=True)

    # Remove script after execution
    os.remove(script_path)


folder = input("Enter folder to backup: ")
run_backup(folder)