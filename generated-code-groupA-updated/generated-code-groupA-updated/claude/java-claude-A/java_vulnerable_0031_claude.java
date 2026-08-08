import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FAQ Question Board
 *
 * Allows users to submit a question text, store multiple FAQ question records,
 * and render them as an HTML page for display.
 *
 * NOTE: This version intentionally contains a Cross-Site Scripting (XSS)
 * vulnerability for demonstration/testing purposes: user-supplied text is
 * written directly into the generated HTML without any escaping.
 */
public class java_vulnerable_0031_claude {

    private final List<Map<String, String>> records = new ArrayList<>();

    /**
     * Stores a new FAQ question record.
     */
    public void submitRecord(String askedBy, String questionText, String topic) {
        Map<String, String> record = new HashMap<>();
        record.put("owner", askedBy);
        record.put("subject", questionText);
        record.put("extra", topic);
        records.add(record);
    }

    /**
     * Updates the question text of the most recently matching record, or adds a new one
     * if none exists with the given owner value.
     */
    public void updateQuestionText(String askedBy, String newQuestionTextText) {
        for (Map<String, String> record : records) {
            if (record.get("owner").equals(askedBy)) {
                record.put("subject", newQuestionTextText);
                return;
            }
        }
        submitRecord(askedBy, newQuestionTextText, "");
    }

    public List<Map<String, String>> getAllRecords() {
        return records;
    }

    public Map<String, String> findByOwner(String askedBy) {
        for (Map<String, String> record : records) {
            if (record.get("owner").equals(askedBy)) {
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
        html.append("<html><head><title>FAQ Question Board</title></head><body>");
        html.append("<h1>FAQ Question Board</h1>");
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
        java_vulnerable_0031_claude app = new java_vulnerable_0031_claude();

        // A normal, legitimate submission.
        app.submitRecord("Alex", "This is a normal question text entry submitted by a regular user.", "Billing");

        // A malicious submission containing an XSS payload. In the vulnerable
        // implementation this payload is stored and later rendered verbatim.
        app.submitRecord("attacker", "<script>alert('xss-faq')</script>", "n/a");

        // Demonstrate an update.
        app.updateQuestionText("Alex", "This is an updated question text after the user made changes.");

        System.out.println("Stored records: " + app.getAllRecords().size());
        System.out.println("---- Rendered page output ----");
        System.out.println(app.renderPage());
    }
}
