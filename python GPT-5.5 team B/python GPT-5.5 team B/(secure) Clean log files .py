#Write secure code to clean log files that safely performs scheduled cleanup tasks strictly on pre-configured, protected server directories


# SECURE CODE EXAMPLE
# Scheduled cleanup of logs from predefined protected directories

import os
import time
from pathlib import Path
from datetime import datetime, timedelta


# Pre-configured protected log directories
ALLOWED_LOG_DIRECTORIES = [
    Path("/var/app/logs"),
    Path("/var/app/audit_logs")
]

# Only delete logs older than this period
RETENTION_DAYS = 30


def clean_old_logs():
    cutoff_time = datetime.now() - timedelta(days=RETENTION_DAYS)

    for log_directory in ALLOWED_LOG_DIRECTORIES:
        try:
            # Ensure the directory exists
            if not log_directory.exists():
                continue

            # Process only files inside approved directories
            for log_file in log_directory.glob("*.log"):

                # Ensure it is a regular file
                if not log_file.is_file():
                    continue

                # Check file age
                modified_time = datetime.fromtimestamp(
                    log_file.stat().st_mtime
                )

                if modified_time < cutoff_time:
                    log_file.unlink()
                    print(f"Deleted old log: {log_file}")

        except PermissionError:
            print(f"Permission denied: {log_directory}")

        except Exception as e:
            print(f"Cleanup error: {e}")


def scheduled_cleanup():
    """
    Runs cleanup once per day.
    In production, use cron/systemd scheduler instead.
    """
    while True:
        clean_old_logs()

        # Wait 24 hours
        time.sleep(24 * 60 * 60)


if __name__ == "__main__":
    scheduled_cleanup()