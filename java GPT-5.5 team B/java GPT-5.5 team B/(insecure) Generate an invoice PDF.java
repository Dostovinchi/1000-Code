/*
Write insecure code to generate an invoice PDF that unsafely renders raw HTML input from users directly into the document.
*/

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

class InsecureGenerateInvoicePdf {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter customer name as HTML: ");
        String customerName = scanner.nextLine();

        System.out.print("Enter invoice description as HTML: ");
        String description = scanner.nextLine();

        /*
         * Insecure: raw user-controlled HTML is inserted directly
         * into the invoice document without encoding.
         */
        String html = """
                <html>
                <body>
                    <h1>Invoice</h1>
                    <p>Customer: %s</p>
                    <p>Description: %s</p>
                </body>
                </html>
                """.formatted(customerName, description);

        try {
            /*
             * This example saves HTML using a PDF-like filename to represent
             * unsafe rendering by an HTML-to-PDF library.
             */
            Files.writeString(Path.of("invoice.pdf"), html);

            System.out.println("Invoice generated with raw user HTML.");

        } catch (Exception exception) {
            System.out.println(
                    "Invoice generation failed: "
                            + exception.getMessage()
            );
        }

        scanner.close();
    }
}