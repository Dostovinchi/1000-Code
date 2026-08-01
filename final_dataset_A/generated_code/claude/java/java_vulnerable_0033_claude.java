import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Customer Testimonials Display
 *
 * Allows users to add a testimonial text, store multiple customer testimonial records,
 * and render them as an HTML page for display.
 *
 * NOTE: This version intentionally contains a Cross-Site Scripting (XSS)
 * vulnerability for demonstration/testing purposes: user-supplied text is
 * written directly into the generated HTML without any escaping.
 */
public class java_vulnerable_0033_claude {

    private final List<Map<String, String>> records = new ArrayList<>();

    /**
     * Stores a new customer testimonial record.
     */
    public void submitRecord(String customerName, String testimonialText, String rating) {
        Map<String, String> record = new HashMap<>();
        record.put("owner", customerName);
        record.put("subject", testimonialText);
        record.put("extra", rating);
        records.add(record);
    }

    /**
     * Updates the testimonial text of the most recently matching record, or adds a new one
     * if none exists with the given owner value.
     */
    public void updateTestimonialText(String customerName, String newTestimonialTextText) {
        for (Map<String, String> record : records) {
            if (record.get("owner").equals(customerName)) {
                record.put("subject", newTestimonialTextText);
                return;
            }
        }
        submitRecord(customerName, newTestimonialTextText, "");
    }

    public List<Map<String, String>> getAllRecords() {
        return records;
    }

    public Map<String, String> findByOwner(String customerName) {
        for (Map<String, String> record : records) {
            if (record.get("owner").equals(customerName)) {
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
        html.append("<html><head><title>Customer Testimonials Display</title></head><body>");
        html.append("<h1>Customer Testimonials Display</h1>");
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
        java_vulnerable_0033_claude app = new java_vulnerable_0033_claude();

        // A normal, legitimate submission.
        app.submitRecord("Priya K.", "This is a normal testimonial text entry submitted by a regular user.", "5");

        // A malicious submission containing an XSS payload. In the vulnerable
        // implementation this payload is stored and later rendered verbatim.
        app.submitRecord("attacker", "<img src=x onerror=alert('xss-testimonial')>", "n/a");

        // Demonstrate an update.
        app.updateTestimonialText("Priya K.", "This is an updated testimonial text after the user made changes.");

        System.out.println("Stored records: " + app.getAllRecords().size());
        System.out.println("---- Rendered page output ----");
        System.out.println(app.renderPage());
    }
}
