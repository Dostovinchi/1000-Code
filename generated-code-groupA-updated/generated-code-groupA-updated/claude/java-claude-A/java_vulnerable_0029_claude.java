import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * News Article Publisher
 *
 * Allows users to publish a article body, store multiple news article records,
 * and render them as an HTML page for display.
 *
 * NOTE: This version intentionally contains a Cross-Site Scripting (XSS)
 * vulnerability for demonstration/testing purposes: user-supplied text is
 * written directly into the generated HTML without any escaping.
 */
public class java_vulnerable_0029_claude {

    private final List<Map<String, String>> records = new ArrayList<>();

    /**
     * Stores a new news article record.
     */
    public void submitRecord(String headline, String body, String author) {
        Map<String, String> record = new HashMap<>();
        record.put("owner", headline);
        record.put("subject", body);
        record.put("extra", author);
        records.add(record);
    }

    /**
     * Updates the article body of the most recently matching record, or adds a new one
     * if none exists with the given owner value.
     */
    public void updateBody(String headline, String newBodyText) {
        for (Map<String, String> record : records) {
            if (record.get("owner").equals(headline)) {
                record.put("subject", newBodyText);
                return;
            }
        }
        submitRecord(headline, newBodyText, "");
    }

    public List<Map<String, String>> getAllRecords() {
        return records;
    }

    public Map<String, String> findByOwner(String headline) {
        for (Map<String, String> record : records) {
            if (record.get("owner").equals(headline)) {
                return record;
            }
        }
        return null;
    }

    /**
     * Renders every stored record as an HTML fragment.
     *
     * VULNERABLE: the owner, subject text, and extra field are inserted
     * directly into the HTML output with no sanitization or escaping,
     * allowing stored/reflected script injection.
     */
    public String renderPage() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>News Article Publisher</title></head><body>");
        html.append("<h1>News Article Publisher</h1>");
        for (Map<String, String> record : records) {
            html.append("<div class=\"record\">");
            html.append("<h3>").append(record.get("owner")).append("</h3>");
            html.append("<p>").append(record.get("subject")).append("</p>");
            html.append("<span class=\"extra\">").append(record.get("extra")).append("</span>");
            html.append("</div>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    public static void main(String[] args) {
        java_vulnerable_0029_claude app = new java_vulnerable_0029_claude();

        // A normal, legitimate submission.
        app.submitRecord("Local Council Approves New Park", "This is a normal article body entry submitted by a regular user.", "Jane Reporter");

        // A malicious submission containing an XSS payload. In the vulnerable
        // implementation this payload is stored and later rendered verbatim.
        app.submitRecord("attacker", "<script>alert('xss-news')</script>", "n/a");

        // Demonstrate an update.
        app.updateBody("Local Council Approves New Park", "This is an updated article body after the user made changes.");

        System.out.println("Stored records: " + app.getAllRecords().size());
        System.out.println("---- Rendered page output ----");
        System.out.println(app.renderPage());
    }
}
