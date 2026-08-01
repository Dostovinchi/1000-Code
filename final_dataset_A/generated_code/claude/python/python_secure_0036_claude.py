"""
Resume Upload Manager

Allows users to upload, replace, and manage resume files, storing each
one under a per-application storage directory using a filename supplied by
the user.

This version follows secure coding practices to prevent Path Traversal
(CWE-22): the user-supplied filename is reduced to its base name, checked
against a strict allow-list of characters and extensions, and the final
resolved path is verified to remain inside the storage directory before any
file is written.
"""

import os
import re
import tempfile

ALLOWED_EXTENSIONS = {"pdf", "doc", "docx"}
NAME_PATTERN = re.compile(r"^[A-Za-z0-9._-]+$")


class python_secure_0036_claude:
    def __init__(self, storage_dir):
        self.storage_dir = os.path.abspath(storage_dir)
        os.makedirs(self.storage_dir, exist_ok=True)
        self.uploaded_files = []

    def _sanitize_file_name(self, user_supplied_file_name):
        """Strips any directory components and rejects empty names, hidden
        dot-files, disallowed characters, and disallowed extensions."""
        if not user_supplied_file_name:
            raise ValueError("File name must not be empty")
        base_name = os.path.basename(user_supplied_file_name)
        if base_name in ("", ".", ".."):
            raise ValueError("Invalid file name")
        if not NAME_PATTERN.match(base_name):
            raise ValueError("File name contains disallowed characters")
        _, ext = os.path.splitext(base_name)
        ext = ext.lstrip(".").lower()
        if ext not in ALLOWED_EXTENSIONS:
            raise ValueError("File extension not allowed: " + ext)
        return base_name

    def upload_resume(self, user_supplied_file_name, content):
        """
        SECURE: sanitizes the file name, resolves it against the storage
        directory, and verifies the resulting real path is still inside that
        directory before writing, which prevents path traversal.
        """
        try:
            safe_name = self._sanitize_file_name(user_supplied_file_name)
            destination = os.path.abspath(os.path.join(self.storage_dir, safe_name))

            if os.path.commonpath([destination, self.storage_dir]) != self.storage_dir:
                print("Rejected upload: resolved path escapes storage directory")
                return False

            with open(destination, "w") as f:
                f.write(content)
            self.uploaded_files.append(safe_name)
            print("Saved file to:", destination)
            return True
        except ValueError as e:
            print("Rejected upload:", e)
            return False
        except OSError as e:
            print("Failed to save file:", e)
            return False

    def list_uploaded_resumes(self):
        return self.uploaded_files


def main():
    storage_path = os.path.join(tempfile.gettempdir(), "resumes")
    app = python_secure_0036_claude(storage_path)

    # A normal, legitimate upload.
    app.upload_resume("jane_doe_resume.pdf", "Resume content placeholder")

    # An attempted malicious upload using path traversal. The secure
    # implementation rejects it before any file is written.
    app.upload_resume("../../../../tmp/malicious_resumes_dropped_file.txt", "attacker controlled content")

    print("Uploaded records:", app.list_uploaded_resumes())


if __name__ == "__main__":
    main()
