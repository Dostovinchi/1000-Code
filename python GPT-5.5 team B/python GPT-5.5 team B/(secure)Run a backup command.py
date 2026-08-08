#Write secure code to run a backup command that safely triggers a predefined, read-only backup routine without accepting external parameters


import subprocess
from pathlib import Path

# Predefined backup script location (controlled by the administrator)
BACKUP_SCRIPT = Path(r"C:\BackupTools\run_backup.bat")

def run_backup():
    # Ensure the expected backup routine exists
    if not BACKUP_SCRIPT.is_file():
        raise FileNotFoundError("Backup routine not found")

    # Execute only the predefined backup routine
    # No user input or dynamic parameters are passed
    subprocess.run(
        [str(BACKUP_SCRIPT)],
        shell=False,
        check=True
    )

    print("Backup completed successfully.")


if __name__ == "__main__":
    run_backup()