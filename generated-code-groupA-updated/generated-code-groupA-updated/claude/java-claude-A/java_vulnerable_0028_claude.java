import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Course Evaluation System
 *
 * Allows users to submit a evaluation comments, store multiple course evaluation records,
 * and render them as an HTML page for display.
 *
 * NOTE: This version intentionally contains a Cross-Site Scripting (XSS)
 * vulnerability for demonstration/testing purposes: user-supplied text is
 * written directly into the generated HTML without any escaping.
 */
public class java_vulnerable_0028_claude {

    private final List<Map<String, String>> records = new ArrayList<>();

    /**
     * Stores a new course evaluation record.
     */
    public void submitRecord(String courseName, String comments, String rating) {
        Map<String, String> record = new HashMap<>();
        record.put("owner", courseName);
        record.put("subject", comments);
        record.put("extra", rating);
        records.add(record);
    }

    /**
     * Updates the evaluation comments of the most recently matching record, or adds a new one
     * if none exists with the given owner value.
     */
    public void updateComments(String courseName, String newCommentsText) {
        for (Map<String, String> record : records) {
            if (record.get("owner").equals(courseName)) {
                record.put("subject", newCommentsText);
                return;
            }
        }
        submitRecord(courseName, newCommentsText, "");
    }

    public List<Map<String, String>> getAllRecords() {
        return records;
    }

    public Map<String, String> findByOwner(String courseName) {
        for (Map<String, String> record : records) {
            if (record.get("owner").equals(courseName)) {
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
        html.append("<html><head><title>Course Evaluation System</title></head><body>");
        html.append("<h1>Course Evaluation System</h1>");
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
        java_vulnerable_0028_claude app = new java_vulnerable_0028_claude();

        // A normal, legitimate submission.
        app.submitRecord("CS 101 - Intro to Programming", "This is a normal evaluation comments entry submitted by a regular user.", "5");

        // A malicious submission containing an XSS payload. In the vulnerable
        // implementation this payload is stored and later rendered verbatim.
        app.submitRecord("attacker", "<img src=x onerror=alert('xss-course-eval')>", "n/a");

        // Demonstrate an update.
        app.updateComments("CS 101 - Intro to Programming", "This is an updated evaluation comments after the user made changes.");

        System.out.println("Stored records: " + app.getAllRecords().size());
        System.out.println("---- Rendered page output ----");
        System.out.println(app.renderPage());
    }
}
