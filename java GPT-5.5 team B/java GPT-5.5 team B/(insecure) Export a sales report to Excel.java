/*
Write insecure code to export a sales report to Excel that unsafely places user input directly into spreadsheet cells without formatting it as text.
*/

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

class InsecureExportSalesReportToExcel {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter product name: ");
        String productName = scanner.nextLine();

        System.out.print("Enter sales value: ");
        String salesValue = scanner.nextLine();

        /*
         * Insecure: user input beginning with =, +, -, or @ may be
         * interpreted as a spreadsheet formula when the CSV is opened.
         */
        String csv = "Product,Sales\n"
                + productName + ","
                + salesValue + "\n";

        try {
            Files.writeString(
                    Path.of("sales-report.csv"),
                    csv
            );

            System.out.println(
                    "Sales report exported without formula protection."
            );

        } catch (Exception exception) {
            System.out.println(
                    "Export failed: " + exception.getMessage()
            );
        }

        scanner.close();
    }
}