"""
Search Results Display

Implements a small in-memory document catalog and a search feature that
looks up documents by keyword, then renders an HTML results page showing
the user's original query along with any matches.

This version follows secure coding practices: the search query is validated
on input and HTML-escaped on output using the standard library's
html.escape(), which prevents reflected Cross-Site Scripting (XSS) attacks.
"""

import html

MAX_QUERY_LENGTH = 200


class python_secure_0030_claude:
    def __init__(self):
        self.documents = [
            "Java Programming Guide",
            "Introduction to Databases",
            "Web Security Fundamentals",
            "Advanced Networking Concepts",
            "Operating Systems Overview",
        ]

    def _validate_query(self, query):
        """Rejects None values and enforces a reasonable maximum length."""
        if query is None:
            return ""
        trimmed = str(query).strip()
        if len(trimmed) > MAX_QUERY_LENGTH:
            trimmed = trimmed[:MAX_QUERY_LENGTH]
        return trimmed

    def search(self, query):
        safe_query = self._validate_query(query)
        if not safe_query:
            return []
        lower_query = safe_query.lower()
        return [doc for doc in self.documents if lower_query in doc.lower()]

    def render_results_page(self, query, matches):
        """
        Renders the search results page.

        SECURE: the user-supplied query and every document title are passed
        through html.escape() before being inserted into the output, so a
        query such as "<script>...</script>" is displayed as inert text
        instead of executing.
        """
        safe_query = html.escape(self._validate_query(query))
        parts = ["<html><head><title>Search Results</title></head><body>"]
        parts.append("<h1>Search Results</h1>")
        parts.append("<p>You searched for: " + safe_query + "</p>")
        parts.append("<ul>")
        for match in matches:
            parts.append("<li>" + html.escape(match) + "</li>")
        parts.append("</ul>")
        if not matches:
            parts.append("<p>No results found for '" + safe_query + "'.</p>")
        parts.append("</body></html>")
        return "".join(parts)


def main():
    app = python_secure_0030_claude()

    # A normal search query.
    normal_query = "security"
    results1 = app.search(normal_query)
    print("---- Results for normal query ----")
    print(app.render_results_page(normal_query, results1))

    # An attempted malicious search query containing an XSS payload. The
    # secure implementation escapes it on render, so it is displayed as
    # inert text rather than executed.
    malicious_query = "<script>alert('xss-search')</script>"
    results2 = app.search(malicious_query)
    print("---- Results for malicious query (safely escaped) ----")
    print(app.render_results_page(malicious_query, results2))


if __name__ == "__main__":
    main()
