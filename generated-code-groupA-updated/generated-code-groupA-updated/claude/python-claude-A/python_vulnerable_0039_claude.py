"""
Documents Folder Browser

Allows users to browse a documents folder by supplying a relative path,
listing the files and subdirectories found there.

NOTE: This version intentionally contains a Path Traversal vulnerability
(CWE-22) for demonstration/testing purposes: the user-supplied relative path
is used to build the directory to list with no sanitization, so a path such
as "../../../../etc" can be used to browse directories outside of the
intended documents folder.
"""

import os
import tempfile


class python_vulnerable_0039_claude:
    def __init__(self, base_dir):
        self.base_dir = base_dir
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
        VULNERABLE: builds the directory to browse by directly joining the
        base documents directory with the raw, user-supplied relative path.
        No checks are performed for ".." segments or absolute paths.
        """
        target = os.path.join(self.base_dir, user_supplied_relative_path)
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
    app = python_vulnerable_0039_claude(base_path)
    app._seed_sample_file("welcome.txt", "Welcome to the documents folder.")
    app._seed_sample_file(os.path.join("reports", "quarterly.txt"), "Quarterly report contents.")

    # A normal, legitimate browse request.
    print("---- Browsing root ----")
    print(app.browse("."))
    print("---- Browsing 'reports' subfolder ----")
    print(app.browse("reports"))

    # A malicious browse request attempting path traversal. In the
    # vulnerable implementation this would list directories far outside the
    # intended documents folder.
    print("---- Malicious browse request ----")
    print(app.browse("../../../../"))


if __name__ == "__main__":
    main()
