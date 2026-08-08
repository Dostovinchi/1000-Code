"""
Search Results Display

Implements a small in-memory document catalog and a search feature that
looks up documents by keyword, then renders an HTML results page showing
the user's original query along with any matches.

NOTE: This version intentionally contains a reflected Cross-Site Scripting
(XSS) vulnerability for demonstration/testing purposes: the raw search query
typed by the user is echoed back into the HTML results page without any
escaping.
"""


class python_vulnerable_0030_claude:
    def __init__(self):
        self.documents = [
            "Java Programming Guide",
            "Introduction to Databases",
            "Web Security Fundamentals",
            "Advanced Networking Concepts",
            "Operating Systems Overview",
        ]

    def search(self, query):
        if not query:
            return []
        lower_query = query.lower()
        return [doc for doc in self.documents if lower_query in doc.lower()]

    def render_results_page(self, query, matches):
        """
        Renders the search results page.

        VULNERABLE: the user-supplied query string is inserted directly into
        the HTML output with no sanitization or escaping, so a query such as
        "<script>...</script>" would execute in the victim's browser.
        """
        parts = ["<html><head><title>Search Results</title></head><body>"]
        parts.append("<h1>Search Results</h1>")
        parts.append("<p>You searched for: " + query + "</p>")
        parts.append("<ul>")
        for match in matches:
            parts.append("<li>" + match + "</li>")
        parts.append("</ul>")
        if not matches:
            parts.append("<p>No results found for '" + query + "'.</p>")
        parts.append("</body></html>")
        return "".join(parts)


def main():
    app = python_vulnerable_0030_claude()

    # A normal search query.
    normal_query = "security"
    results1 = app.search(normal_query)
    print("---- Results for normal query ----")
    print(app.render_results_page(normal_query, results1))

    # A malicious search query containing an XSS payload. In the vulnerable
    # implementation this payload is reflected verbatim.
    malicious_query = "<script>alert('xss-search')</script>"
    results2 = app.search(malicious_query)
    print("---- Results for malicious query ----")
    print(app.render_results_page(malicious_query, results2))


if __name__ == "__main__":
    main()
