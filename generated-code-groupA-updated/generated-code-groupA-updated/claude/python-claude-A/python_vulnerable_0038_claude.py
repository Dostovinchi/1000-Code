"""
Server Log Viewer

Allows users to request and retrieve server log files by name from a
per-application storage directory, and to view a listing of what is
currently available.

NOTE: This version intentionally contains a Path Traversal vulnerability
(CWE-22) for demonstration/testing purposes: the user-supplied file name is
used to build the read path with no sanitization, so a name such as
"../../../../etc/passwd" or an absolute path can be used to read files
outside of the intended storage directory.
"""

import os
import tempfile


class python_vulnerable_0038_claude:
    def __init__(self, storage_dir):
        self.storage_dir = storage_dir
        os.makedirs(self.storage_dir, exist_ok=True)

    def _seed_sample_file(self, name, content):
        try:
            with open(os.path.join(self.storage_dir, name), "w") as f:
                f.write(content)
        except OSError as e:
            print("Could not seed sample file:", e)

    def list_log_files(self):
        try:
            return [f for f in os.listdir(self.storage_dir)
                    if os.path.isfile(os.path.join(self.storage_dir, f))]
        except OSError:
            return []

    def fetch_log_file(self, user_supplied_file_name):
        """
        VULNERABLE: builds the read path by directly joining the storage
        directory with the raw, user-supplied file name. No checks are
        performed for ".." segments or absolute paths, allowing an attacker
        to read arbitrary files on the system.
        """
        target = os.path.join(self.storage_dir, user_supplied_file_name)
        try:
            with open(target, "r") as f:
                return f.read()
        except OSError as e:
            return "Error reading file: " + str(e)


def main():
    storage_path = os.path.join(tempfile.gettempdir(), "server_logs")
    app = python_vulnerable_0038_claude(storage_path)
    app._seed_sample_file("app-2026-07-19.log", "INFO 2026-07-19 08:00:00 Application started successfully")

    print("Available files:", app.list_log_files())

    # A normal, legitimate request.
    print("---- Requested file content ----")
    print(app.fetch_log_file("app-2026-07-19.log"))

    # A malicious request attempting path traversal. In the vulnerable
    # implementation this would read whatever file the attacker specifies,
    # wherever it lives on disk.
    malicious_request = "../../../../etc/passwd"
    print("---- Malicious request result ----")
    print(app.fetch_log_file(malicious_request))


if __name__ == "__main__":
    main()
