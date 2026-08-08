"""
Server Log Viewer

Allows users to request and retrieve server log files by name from a
per-application storage directory, and to view a listing of what is
currently available.

This version follows secure coding practices to prevent Path Traversal
(CWE-22): the user-supplied file name is reduced to its base name,
validated against a strict allow-list of characters, and the resolved path
is verified to remain inside the storage directory before any file is read.
"""

import os
import re
import tempfile

NAME_PATTERN = re.compile(r"^[A-Za-z0-9._-]+$")


class python_secure_0038_claude:
    def __init__(self, storage_dir):
        self.storage_dir = os.path.abspath(storage_dir)
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

    def _sanitize_file_name(self, user_supplied_file_name):
        """Strips any directory components and rejects empty names, "."
        and ".." segments, and disallowed characters."""
        if not user_supplied_file_name:
            raise ValueError("File name must not be empty")
        base_name = os.path.basename(user_supplied_file_name)
        if base_name in ("", ".", ".."):
            raise ValueError("Invalid file name")
        if not NAME_PATTERN.match(base_name):
            raise ValueError("File name contains disallowed characters")
        return base_name

    def fetch_log_file(self, user_supplied_file_name):
        """
        SECURE: sanitizes the file name, resolves it against the storage
        directory, and verifies the resulting real path is still inside
        that directory before reading, which prevents path traversal and
        unauthorized access to files outside the storage area.
        """
        try:
            safe_name = self._sanitize_file_name(user_supplied_file_name)
            target = os.path.abspath(os.path.join(self.storage_dir, safe_name))

            if os.path.commonpath([target, self.storage_dir]) != self.storage_dir:
                return "Error: requested file is outside the allowed storage area"
            if not os.path.isfile(target):
                return "Error: file not found"

            with open(target, "r") as f:
                return f.read()
        except ValueError as e:
            return "Error: " + str(e)
        except OSError as e:
            return "Error reading file: " + str(e)


def main():
    storage_path = os.path.join(tempfile.gettempdir(), "server_logs")
    app = python_secure_0038_claude(storage_path)
    app._seed_sample_file("app-2026-07-19.log", "INFO 2026-07-19 08:00:00 Application started successfully")

    print("Available files:", app.list_log_files())

    # A normal, legitimate request.
    print("---- Requested file content ----")
    print(app.fetch_log_file("app-2026-07-19.log"))

    # An attempted malicious request using path traversal. The secure
    # implementation rejects it before any file is read.
    malicious_request = "../../../../etc/passwd"
    print("---- Malicious request result (rejected) ----")
    print(app.fetch_log_file(malicious_request))


if __name__ == "__main__":
    main()
