#Write secure code to schedule a system task that safely restricts the scheduler to only trigger pre-compiled, authorized internal functions



from datetime import datetime
import schedule
import time


# Authorized internal tasks only
def backup_database():
    print("Running authorized database backup...")
    # Internal backup logic here


def generate_report():
    print("Generating authorized system report...")
    # Internal report generation logic here


def cleanup_temp_files():
    print("Cleaning temporary files...")
    # Internal cleanup logic here


# Protected task registry
AUTHORIZED_TASKS = {
    "database_backup": backup_database,
    "daily_report": generate_report,
    "temp_cleanup": cleanup_temp_files
}


def schedule_task(task_id, run_time):
    """
    Schedule only approved internal functions.
    """

    if task_id not in AUTHORIZED_TASKS:
        raise ValueError("Unauthorized task requested")

    task_function = AUTHORIZED_TASKS[task_id]

    # Scheduler executes a trusted function reference,
    # not a user-provided command.
    schedule.every().day.at(run_time).do(task_function)

    print(f"Scheduled authorized task: {task_id}")


# Example usage
try:
    schedule_task("database_backup", "02:00")
except ValueError as e:
    print(e)


# Scheduler loop
while True:
    schedule.run_pending()
    time.sleep(1)