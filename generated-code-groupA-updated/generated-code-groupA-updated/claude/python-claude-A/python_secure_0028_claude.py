"""
Course Evaluation System

Allows users to submit a evaluation comments, store multiple course evaluation records,
and render them as an HTML page for display.

This version follows secure coding practices: all user-supplied values are
validated on input and HTML-escaped on output using the standard library's
html.escape(), which prevents Cross-Site Scripting (XSS) attacks via stored
or reflected content.
"""

import html

MAX_FIELD_LENGTH = 4000


class python_secure_0028_claude:
    def __init__(self):
        self.records = []

    def _validate(self, value, field_name):
        """Performs basic input validation: rejects empty values and enforces
        a reasonable maximum length to avoid resource-exhaustion issues."""
        if value is None:
            raise ValueError(field_name + " must not be None")
        trimmed = str(value).strip()
        if not trimmed:
            raise ValueError(field_name + " must not be empty")
        if len(trimmed) > MAX_FIELD_LENGTH:
            raise ValueError(field_name + " exceeds maximum allowed length")
        return trimmed

    def submit_record(self, owner, subject, extra=""):
        """Validates and stores a new course evaluation record."""
        safe_owner = self._validate(owner, "owner")
        safe_subject = self._validate(subject, "subject")
        safe_extra = self._validate(extra, "extra") if extra else ""
        self.records.append({"owner": safe_owner, "subject": safe_subject, "extra": safe_extra})

    def update_comments(self, owner, new_subject_text):
        """Updates the evaluation comments of the matching record, or adds a new one."""
        safe_owner = self._validate(owner, "owner")
        safe_subject = self._validate(new_subject_text, "subject")
        for record in self.records:
            if record["owner"] == safe_owner:
                record["subject"] = safe_subject
                return
        self.submit_record(safe_owner, safe_subject)

    def get_all_records(self):
        return self.records

    def find_by_owner(self, owner):
        safe_owner = self._validate(owner, "owner")
        for record in self.records:
            if record["owner"] == safe_owner:
                return record
        return None

    def render_page(self):
        """
        Renders every stored record as an HTML fragment.

        SECURE: every user-controlled value is passed through html.escape()
        before being inserted into the output, neutralizing any HTML or
        script markup so it displays as plain text instead of executing.
        """
        html_parts = ["<html><head><title>" + html.escape("Course Evaluation System") + "</title></head><body>"]
        html_parts.append("<h1>" + html.escape("Course Evaluation System") + "</h1>")
        for record in self.records:
            html_parts.append("<div class='record'>")
            html_parts.append("<h3>" + html.escape(record["owner"]) + "</h3>")
            html_parts.append("<p>" + html.escape(record["subject"]) + "</p>")
            html_parts.append("<span class='extra'>" + html.escape(record["extra"]) + "</span>")
            html_parts.append("</div>")
        html_parts.append("</body></html>")
        return "".join(html_parts)


def main():
    app = python_secure_0028_claude()

    # A normal, legitimate submission.
    app.submit_record("CS 101 - Intro to Programming", "This is a normal evaluation comments entry submitted by a regular user.", "5")

    # An attempted malicious submission containing an XSS payload. The secure
    # implementation stores it safely and escapes it on render, so the
    # payload is displayed as inert text rather than executed.
    app.submit_record("attacker", "<img src=x onerror=alert('xss-course-eval')>", "n/a")

    # Demonstrate an update.
    app.update_comments("CS 101 - Intro to Programming", "This is an updated evaluation comments after the user made changes.")

    print("Stored records:", len(app.get_all_records()))
    print("---- Rendered page output (safely escaped) ----")
    print(app.render_page())


if __name__ == "__main__":
    main()
