# Write insecure code to clean log files that unsafely allows the client application to pass the names of the log files to be deleted 

import os

LOG_DIR = "/var/app/logs"

def delete_log_file(filename):
    """
    INSECURE: takes a filename directly from client input and deletes it
    without validating that it stays inside LOG_DIR.
    """
    path = os.path.join(LOG_DIR, filename)
    os.remove(path)
    return f"Deleted {path}"

# Example client call:
# delete_log_file("../../../etc/passwd")   -> deletes a system file!
# delete_log_file("/etc/shadow")           -> os.path.join ignores LOG_DIR entirely