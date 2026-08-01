"""
Documents Folder Browser

Allows users to browse a documents folder by supplying a relative path,
listing the files and subdirectories found there.

This version follows secure coding practices to prevent Path Traversal
(CWE-22): the user-supplied relative path is normalized and the resulting
real directory is verified to remain inside the base documents folder
before it is listed.
"""

import os
import tempfile


class python_secure_0039_claude:
    def __init__(self, base_dir):
        self.base_dir = os.path.abspath(base_dir)
        os.makedirs(self.base_dir, exist_ok=True)

    def _seed_sample_file(self, relative_path, content):
        try:
            full_path = os.path.join(self.base_dir, relative_path)
            os.makedirs(os.path.dirname(full_path), exist_ok=True)
            with open(full_path, "w") as f:
                f.write(content)
        except OSError as e:
            print("Could not seed sample file:", e)

    def browse(self, user_supplied_relative_path):
        """
        SECURE: normalizes the user-supplied relative path and resolves it
        against the base documents directory, then verifies the resulting
        real path is still inside that directory before listing its
        contents, which prevents path traversal outside the intended
        folder.
        """
        if not user_supplied_relative_path:
            user_supplied_relative_path = "."

        target = os.path.abspath(os.path.join(self.base_dir, user_supplied_relative_path))

        if os.path.commonpath([target, self.base_dir]) != self.base_dir:
            return ["Error: requested path is outside the allowed documents folder"]

        try:
            names = os.listdir(target)
        except OSError:
            return ["Error: cannot list '" + user_supplied_relative_path + "'"]

        entries = []
        for name in names:
            full = os.path.join(target, name)
            entries.append(("[DIR] " if os.path.isdir(full) else "[FILE] ") + name)
        return entries


def main():
    base_path = os.path.join(tempfile.gettempdir(), "documents_browser_demo")
    app = python_secure_0039_claude(base_path)
    app._seed_sample_file("welcome.txt", "Welcome to the documents folder.")
    app._seed_sample_file(os.path.join("reports", "quarterly.txt"), "Quarterly report contents.")

    # A normal, legitimate browse request.
    print("---- Browsing root ----")
    print(app.browse("."))
    print("---- Browsing 'reports' subfolder ----")
    print(app.browse("reports"))

    # An attempted malicious browse request using path traversal. The
    # secure implementation rejects it before listing anything.
    print("---- Malicious browse request (rejected) ----")
    print(app.browse("../../../../"))


if __name__ == "__main__":
    main()


