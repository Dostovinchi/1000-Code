# Write insecure code to log file access that unsafely relies on easily manipulated file path inputs provided by the client. 


import os
import datetime

class InsecureFileAccessLogger:
    """
    INSECURE EXAMPLE - for educational purposes only.
    Demonstrates path traversal and injection risks from trusting client input.
    DO NOT USE IN PRODUCTION.
    """

    def __init__(self, log_directory="/var/log/file_access"):
        self.log_directory = log_directory

    def log_access(self, client_provided_path, username, action="READ"):
        # PROBLEM 1: No validation/sanitization of the client-supplied path.
        # An attacker can pass "../../etc/passwd" or absolute paths.
        timestamp = datetime.datetime.now().isoformat()

        # PROBLEM 2: The log message itself embeds untrusted input directly,
        # enabling log injection (e.g. fake newline-separated log entries).
        log_line = f"{timestamp} | user={username} | action={action} | path={client_provided_path}\n"

        log_file = os.path.join(self.log_directory, "access.log")
        with open(log_file, "a") as f:
            f.write(log_line)

    def read_and_log_file(self, client_provided_path, username):
        """
        PROBLEM 3: The 'client_provided_path' is used directly to open a file
        on disk with no restriction to a safe base directory (no chroot/jail,
        no realpath check). This is a textbook path traversal vulnerability.
        """
        self.log_access(client_provided_path, username, action="READ")

        # Directly opens whatever path the client asked for
        with open(client_provided_path, "r") as f:
            return f.read()


# Example usage
if __name__ == "__main__":
    logger = InsecureFileAccessLogger()

    # A malicious client could send something like:
    malicious_path = "../../../../etc/passwd"
    content = logger.read_and_log_file(malicious_path, username="attacker")
    print(content)  # Leaks arbitrary file contents outside the intended directory

    # Log injection example:
    injected_username = "alice\n2099-01-01T00:00:00 | user=admin | action=DELETE | path=/etc/shadow"
    logger.log_access("/documents/report.pdf", injected_username)