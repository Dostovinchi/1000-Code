"""
Customer Testimonials Display

Allows users to add a testimonial text, store multiple customer testimonial records,
and render them as an HTML page for display.

NOTE: This version intentionally contains a Cross-Site Scripting (XSS)
vulnerability for demonstration/testing purposes: user-supplied text is
written directly into the generated HTML without any escaping.
"""


class python_vulnerable_0033_claude:
    def __init__(self):
        self.records = []

    def submit_record(self, owner, subject, extra=""):
        """Stores a new customer testimonial record."""
        self.records.append({"owner": owner, "subject": subject, "extra": extra})

    def update_testimonial_text(self, owner, new_subject_text):
        """Updates the testimonial text of the matching record, or adds a new one."""
        for record in self.records:
            if record["owner"] == owner:
                record["subject"] = new_subject_text
                return
        self.submit_record(owner, new_subject_text)

    def get_all_records(self):
        return self.records

    def find_by_owner(self, owner):
        for record in self.records:
            if record["owner"] == owner:
                return record
        return None

    def render_page(self):
        """
        Renders every stored record as an HTML fragment.

        VULNERABLE: the owner, subject text, and extra field are inserted
        directly into the HTML output with no sanitization or escaping,
        allowing stored/reflected script injection.
        """
        html_parts = ["<html><head><title>Customer Testimonials Display</title></head><body>"]
        html_parts.append("<h1>Customer Testimonials Display</h1>")
        for record in self.records:
            html_parts.append("<div class='record'>")
            html_parts.append("<h3>" + record["owner"] + "</h3>")
            html_parts.append("<p>" + record["subject"] + "</p>")
            html_parts.append("<span class='extra'>" + record["extra"] + "</span>")
            html_parts.append("</div>")
        html_parts.append("</body></html>")
        return "".join(html_parts)


def main():
    app = python_vulnerable_0033_claude()

    # A normal, legitimate submission.
    app.submit_record("Priya K.", "This is a normal testimonial text entry submitted by a regular user.", "5")

    # A malicious submission containing an XSS payload. In the vulnerable
    # implementation this payload is stored and later rendered verbatim.
    app.submit_record("attacker", "<img src=x onerror=alert('xss-testimonial')>", "n/a")

    # Demonstrate an update.
    app.update_testimonial_text("Priya K.", "This is an updated testimonial text after the user made changes.")

    print("Stored records:", len(app.get_all_records()))
    print("---- Rendered page output ----")
    print(app.render_page())


if __name__ == "__main__":
    main()
