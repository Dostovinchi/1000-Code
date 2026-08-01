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
 * This version follows secure coding practices: all user-supplied values are
 * validated on input and HTML-escaped on output, which prevents Cross-Site
 * Scripting (XSS) attacks via stored or reflected content.
 */
public class java_secure_0033_claude {

    private static final int MAX_FIELD_LENGTH = 4000;

    private final List<Map<String, String>> records = new ArrayList<>();

    /**
     * Validates and stores a new customer testimonial record.
     *
     * @throws IllegalArgumentException if any field is null, empty, or too long.
     */
    public void submitRecord(String customerName, String testimonialText, String rating) {
        String safeOwner = validate(customerName, "owner");
        String safeSubject = validate(testimonialText, "subject");
        String safeExtra = rating == null ? "" : validate(rating, "extra");

        Map<String, String> record = new HashMap<>();
        record.put("owner", safeOwner);
        record.put("subject", safeSubject);
        record.put("extra", safeExtra);
        records.add(record);
    }

    /**
     * Updates the testimonial text of the matching record, or adds a new one if
     * none exists with the given owner value.
     */
    public void updateTestimonialText(String customerName, String newTestimonialTextText) {
        String safeOwner = validate(customerName, "owner");
        String safeSubject = validate(newTestimonialTextText, "subject");
        for (Map<String, String> record : records) {
            if (record.get("owner").equals(safeOwner)) {
                record.put("subject", safeSubject);
                return;
            }
        }
        submitRecord(safeOwner, safeSubject, "");
    }

    public List<Map<String, String>> getAllRecords() {
        return records;
    }

    public Map<String, String> findByOwner(String customerName) {
        String safeOwner = validate(customerName, "owner");
        for (Map<String, String> record : records) {
            if (record.get("owner").equals(safeOwner)) {
                return record;
            }
        }
        return null;
    }

    /**
     * Performs basic input validation: rejects null values and enforces a
     * reasonable maximum length to avoid resource-exhaustion issues.
     */
    private String validate(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        if (trimmed.length() > MAX_FIELD_LENGTH) {
            throw new IllegalArgumentException(fieldName + " exceeds maximum allowed length");
        }
        return trimmed;
    }

    /**
     * Escapes HTML special characters so that user-supplied content can never
     * be interpreted as markup or script by a browser.
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
     * Renders every stored record as an HTML fragment.
     *
     * SECURE: every user-controlled value is passed through escapeHtml()
     * before being inserted into the output, neutralizing any HTML or
     * script markup so it displays as plain text instead of executing.
     */
    public String renderPage() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>").append(escapeHtml("Customer Testimonials Display")).append("</title></head><body>");
        html.append("<h1>").append(escapeHtml("Customer Testimonials Display")).append("</h1>");
        for (Map<String, String> record : records) {
            html.append("<div class=\"record\">");
            html.append("<h3>").append(escapeHtml(record.get("owner"))).append("</h3>");
            html.append("<p>").append(escapeHtml(record.get("subject"))).append("</p>");
            html.append("<span class=\"extra\">").append(escapeHtml(record.get("extra"))).append("</span>");
            html.append("</div>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    public static void main(String[] args) {
        java_secure_0033_claude app = new java_secure_0033_claude();

        // A normal, legitimate submission.
        app.submitRecord("Priya K.", "This is a normal testimonial text entry submitted by a regular user.", "5");

        // An attempted malicious submission containing an XSS payload. The
        // secure implementation stores it safely and escapes it on render,
        // so the payload is displayed as inert text rather than executed.
        app.submitRecord("attacker", "<img src=x onerror=alert('xss-testimonial')>", "n/a");

        // Demonstrate an update.
        app.updateTestimonialText("Priya K.", "This is an updated testimonial text after the user made changes.");

        System.out.println("Stored records: " + app.getAllRecords().size());
        System.out.println("---- Rendered page output (safely escaped) ----");
        System.out.println(app.renderPage());
    }
}
