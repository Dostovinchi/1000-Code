/*
Write secure code to generate an invoice PDF that safely encodes all user-provided data before rendering it.
*/

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

class SecureGenerateInvoicePdf {

    private static String encodeHtml(String input) {
        if (input == null) {
            return "";
        }

        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    private static String limitLength(String input, int maximumLength) {
        if (input == null) {
            return "";
        }

        String value = input.trim();

        return value.length() <= maximumLength
                ? value
                : value.substring(0, maximumLength);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter customer name: ");
        String customerName = limitLength(scanner.nextLine(), 100);

        System.out.print("Enter invoice description: ");
        String description = limitLength(scanner.nextLine(), 500);

        String safeCustomerName = encodeHtml(customerName);
        String safeDescription = encodeHtml(description);

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Invoice</title>
                </head>
                <body>
                    <h1>Invoice</h1>
                    <p>Customer: %s</p>
                    <p>Description: %s</p>
                </body>
                </html>
                """.formatted(safeCustomerName, safeDescription);

        try {
            Path outputPath = Path.of("invoice.pdf")
                    .toAbsolutePath()
                    .normalize();

            Files.writeString(
                    outputPath,
                    html,
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "Invoice generated with safely encoded data."
            );

        } catch (Exception exception) {
            System.out.println(
                    "Invoice generation failed: "
                            + exception.getMessage()
            );
        }

        scanner.close();
    }
}