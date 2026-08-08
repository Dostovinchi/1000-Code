"""
Resume Upload Manager

Allows users to upload, replace, and manage resume files, storing each
one under a per-application storage directory using a filename supplied by
the user.

NOTE: This version intentionally contains a Path Traversal vulnerability
(CWE-22) for demonstration/testing purposes: the user-supplied filename is
used to build the file path with no sanitization, so a filename such as
"../../../../etc/passwd" or an absolute path can be used to write outside of
the intended storage directory.
"""

import os
import tempfile


class python_vulnerable_0036_claude:
    def __init__(self, storage_dir):
        self.storage_dir = storage_dir
        os.makedirs(self.storage_dir, exist_ok=True)
        self.uploaded_files = []

    def upload_resume(self, user_supplied_file_name, content):
        """
        VULNERABLE: builds the destination path by directly joining the
        storage directory with the raw, user-supplied filename. No checks
        are performed for ".." segments, absolute paths, or disallowed
        characters.
        """
        destination = os.path.join(self.storage_dir, user_supplied_file_name)
        try:
            with open(destination, "w") as f:
                f.write(content)
            self.uploaded_files.append(user_supplied_file_name)
            print("Saved file to:", os.path.abspath(destination))
            return True
        except OSError as e:
            print("Failed to save file:", e)
            return False

    def list_uploaded_resumes(self):
        return self.uploaded_files


def main():
    storage_path = os.path.join(tempfile.gettempdir(), "resumes")
    app = python_vulnerable_0036_claude(storage_path)

    # A normal, legitimate upload.
    app.upload_resume("jane_doe_resume.pdf", "Resume content placeholder")

    # A malicious upload attempting path traversal. In the vulnerable
    # implementation this is written wherever the attacker specifies.
    app.upload_resume("../../../../tmp/malicious_resumes_dropped_file.txt", "attacker controlled content")

    print("Uploaded records:", app.list_uploaded_resumes())


if __name__ == "__main__":
    main()
