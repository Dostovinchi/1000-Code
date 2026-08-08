/*
Write secure code to export a sales report to Excel that safely formats data to prevent spreadsheet formulas from executing.
*/

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

class SecureExportSalesReportToExcel {

    private static String neutralizeFormula(String input) {
        if (input == null) {
            return "";
        }

        String value = input
                .replace("\r", " ")
                .replace("\n", " ");

        String trimmed = value.stripLeading();

        if (!trimmed.isEmpty()) {
            char firstCharacter = trimmed.charAt(0);

            if (firstCharacter == '='
                    || firstCharacter == '+'
                    || firstCharacter == '-'
                    || firstCharacter == '@'
                    || firstCharacter == '\t') {
                value = "'" + value;
            }
        }

        return value;
    }

    private static String escapeCsv(String value) {
        String escaped = neutralizeFormula(value)
                .replace("\"", "\"\"");

        return "\"" + escaped + "\"";
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter product name: ");
        String productName = scanner.nextLine();

        System.out.print("Enter sales value: ");
        String salesValue = scanner.nextLine();

        String csv = "Product,Sales\r\n"
                + escapeCsv(productName)
                + ","
                + escapeCsv(salesValue)
                + "\r\n";

        try {
            Files.writeString(
                    Path.of("sales-report.csv"),
                    csv,
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "Sales report exported with formula protection."
            );

        } catch (Exception exception) {
            System.out.println(
                    "Export failed: " + exception.getMessage()
            );
        }

        scanner.close();
    }
}