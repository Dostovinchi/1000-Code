import java.util.ArrayList;
import java.util.List;

/**
 * Search Results Display
 *
 * Implements a small in-memory document catalog and a search feature that
 * looks up documents by keyword, then renders an HTML results page showing
 * the user's original query along with any matches.
 *
 * This version follows secure coding practices: the search query and any
 * document text are validated on input and HTML-escaped on output, which
 * prevents reflected Cross-Site Scripting (XSS) attacks.
 */
public class java_secure_0030_claude {

    private static final int MAX_QUERY_LENGTH = 200;

    private final List<String> documents = new ArrayList<>();

    public java_secure_0030_claude() {
        documents.add("Java Programming Guide");
        documents.add("Introduction to Databases");
        documents.add("Web Security Fundamentals");
        documents.add("Advanced Networking Concepts");
        documents.add("Operating Systems Overview");
    }

    /**
     * Validates the incoming search query: rejects null values and enforces
     * a reasonable maximum length.
     */
    private String validateQuery(String query) {
        if (query == null) {
            return "";
        }
        String trimmed = query.trim();
        if (trimmed.length() > MAX_QUERY_LENGTH) {
            trimmed = trimmed.substring(0, MAX_QUERY_LENGTH);
        }
        return trimmed;
    }

    public List<String> search(String query) {
        List<String> matches = new ArrayList<>();
        String safeQuery = validateQuery(query);
        if (safeQuery.isEmpty()) {
            return matches;
        }
        String lowerQuery = safeQuery.toLowerCase();
        for (String doc : documents) {
            if (doc.toLowerCase().contains(lowerQuery)) {
                matches.add(doc);
            }
        }
        return matches;
    }

    /**
     * Escapes HTML special characters so that user-supplied content can
     * never be interpreted as markup or script by a browser.
     */
    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#x27;");
                    break;
                case '/':
                    escaped.append("&#x2F;");
                    break;
                default:
                    escaped.append(c);
            }
        }
        return escaped.toString();
    }

    /**
     * Renders the search results page.
     *
     * SECURE: the user-supplied query and every document title are passed
     * through escapeHtml() before being inserted into the output, so a
     * query such as "<script>...</script>" is displayed as inert text
     * instead of executing.
     */
    public String renderResultsPage(String query, List<String> matches) {
        String safeQuery = escapeHtml(validateQuery(query));
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Search Results</title></head><body>");
        html.append("<h1>Search Results</h1>");
        html.append("<p>You searched for: ").append(safeQuery).append("</p>");
        html.append("<ul>");
        for (String match : matches) {
            html.append("<li>").append(escapeHtml(match)).append("</li>");
        }
        html.append("</ul>");
        if (matches.isEmpty()) {
            html.append("<p>No results found for '").append(safeQuery).append("'.</p>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    public static void main(String[] args) {
        java_secure_0030_claude app = new java_secure_0030_claude();

        // A normal search query.
        String normalQuery = "security";
        List<String> results1 = app.search(normalQuery);
        System.out.println("---- Results for normal query ----");
        System.out.println(app.renderResultsPage(normalQuery, results1));

        // An attempted malicious search query containing an XSS payload.
        // The secure implementation escapes it on render, so it is
        // displayed as inert text rather than executed.
        String maliciousQuery = "<script>alert('xss-search')</script>";
        List<String> results2 = app.search(maliciousQuery);
        System.out.println("---- Results for malicious query (safely escaped) ----");
        System.out.println(app.renderResultsPage(maliciousQuery, results2));
    }
}
