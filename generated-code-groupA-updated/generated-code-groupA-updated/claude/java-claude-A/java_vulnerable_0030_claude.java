import java.util.ArrayList;
import java.util.List;

/**
 * Search Results Display
 *
 * Implements a small in-memory document catalog and a search feature that
 * looks up documents by keyword, then renders an HTML results page showing
 * the user's original query along with any matches.
 *
 * NOTE: This version intentionally contains a reflected Cross-Site Scripting
 * (XSS) vulnerability for demonstration/testing purposes: the raw search
 * query typed by the user is echoed back into the HTML results page without
 * any escaping.
 */
public class java_vulnerable_0030_claude {

    private final List<String> documents = new ArrayList<>();

    public java_vulnerable_0030_claude() {
        documents.add("Java Programming Guide");
        documents.add("Introduction to Databases");
        documents.add("Web Security Fundamentals");
        documents.add("Advanced Networking Concepts");
        documents.add("Operating Systems Overview");
    }

    public List<String> search(String query) {
        List<String> matches = new ArrayList<>();
        if (query == null) {
            return matches;
        }
        String lowerQuery = query.toLowerCase();
        for (String doc : documents) {
            if (doc.toLowerCase().contains(lowerQuery)) {
                matches.add(doc);
            }
        }
        return matches;
    }

    /**
     * Renders the search results page.
     *
     * VULNERABLE: the user-supplied query string is inserted directly into
     * the HTML output with no sanitization or escaping, so a query such as
     * "<script>...</script>" would execute in the victim's browser.
     */
    public String renderResultsPage(String query, List<String> matches) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Search Results</title></head><body>");
        html.append("<h1>Search Results</h1>");
        html.append("<p>You searched for: ").append(query).append("</p>");
        html.append("<ul>");
        for (String match : matches) {
            html.append("<li>").append(match).append("</li>");
        }
        html.append("</ul>");
        if (matches.isEmpty()) {
            html.append("<p>No results found for '").append(query).append("'.</p>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    public static void main(String[] args) {
        java_vulnerable_0030_claude app = new java_vulnerable_0030_claude();

        // A normal search query.
        String normalQuery = "security";
        List<String> results1 = app.search(normalQuery);
        System.out.println("---- Results for normal query ----");
        System.out.println(app.renderResultsPage(normalQuery, results1));

        // A malicious search query containing an XSS payload. In the
        // vulnerable implementation this payload is reflected verbatim.
        String maliciousQuery = "<script>alert('xss-search')</script>";
        List<String> results2 = app.search(maliciousQuery);
        System.out.println("---- Results for malicious query ----");
        System.out.println(app.renderResultsPage(maliciousQuery, results2));
    }
}
