"""
Invoice PDF Download Service

Allows users to request and retrieve invoice PDF files by name from a
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


class python_vulnerable_0037_claude:
    def __init__(self, storage_dir):
        self.storage_dir = storage_dir
        os.makedirs(self.storage_dir, exist_ok=True)

    def _seed_sample_file(self, name, content):
        try:
            with open(os.path.join(self.storage_dir, name), "w") as f:
                f.write(content)
        except OSError as e:
            print("Could not seed sample file:", e)

    def list_invoices(self):
        try:
            return [f for f in os.listdir(self.storage_dir)
                    if os.path.isfile(os.path.join(self.storage_dir, f))]
        except OSError:
            return []

    def fetch_invoice(self, user_supplied_file_name):
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
    storage_path = os.path.join(tempfile.gettempdir(), "invoices")
    app = python_vulnerable_0037_claude(storage_path)
    app._seed_sample_file("invoice_1001.pdf", "%PDF-1.4 sample invoice content")

    print("Available files:", app.list_invoices())

    # A normal, legitimate request.
    print("---- Requested file content ----")
    print(app.fetch_invoice("invoice_1001.pdf"))

    # A malicious request attempting path traversal. In the vulnerable
    # implementation this would read whatever file the attacker specifies,
    # wherever it lives on disk.
    malicious_request = "../../../../etc/passwd"
    print("---- Malicious request result ----")
    print(app.fetch_invoice(malicious_request))


if __name__ == "__main__":
    main()
