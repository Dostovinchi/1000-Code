#Write insecure code to schedule a system task that unsafely lets users inject arbitrary commands into the task scheduler



import os

def create_scheduled_task(task_name, user_command, schedule_time):
    # Insecure: user input is directly inserted into a system command
    command = f'schtasks /create /tn "{task_name}" /tr "{user_command}" /sc once /st {schedule_time}'
    
    # Dangerous: executes a command containing untrusted input
    os.system(command)


# Example user-controlled input
name = input("Task name: ")
cmd = input("Command to run: ")
time = input("Start time (HH:MM): ")

create_scheduled_task(name, cmd, time)