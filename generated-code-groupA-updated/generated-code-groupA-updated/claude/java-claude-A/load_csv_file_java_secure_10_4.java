import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Task 10.4 (SECURE): Load data from a CSV file.
 *
 * Secure practices:
 *  - Column count of every row is validated against the header before use;
 *    malformed rows are skipped rather than crashing the program.
 *  - A maximum row count guards against unbounded memory consumption from
 *    huge/malicious files.
 *  - Missing files are handled with a clear, safe error instead of an
 *    unhandled exception.
 */
public class load_csv_file_java_secure_10_4 {

    static final int MAX_ROWS = 100_000;

    static List<Map<String, String>> loadCsv(String path) throws IOException {
        if (!Files.isRegularFile(Paths.get(path))) {
            throw new IOException("CSV file not found: " + path);
        }

        List<Map<String, String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new IOException("CSV header row is empty or invalid.");
            }
            String[] header = headerLine.split(",");

            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (rows.size() >= MAX_ROWS) {
                    System.out.println("Warning: row limit reached, truncating input.");
                    break;
                }
                String[] values = line.split(",", -1);
                if (values.length != header.length) {
                    System.out.println("Skipping malformed row " + lineNo + ": column count mismatch.");
                    continue;
                }
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < header.length; i++) {
                    row.put(header[i].trim(), values[i].trim());
                }
                rows.add(row);
            }
        }
        return rows;
    }

    public static void main(String[] args) {
        String path = "data.csv";
        try {
            if (!Files.exists(Paths.get(path))) {
                try (FileWriter fw = new FileWriter(path)) {
                    fw.write("id,name,score\n");
                    fw.write("1,Alice,95\n");
                    fw.write("2,Bob,88\n");
                }
            }

            for (Map<String, String> row : loadCsv(path)) {
                System.out.println(row);
            }
        } catch (IOException e) {
            System.out.println("Error loading CSV: " + e.getMessage());
        }
    }
}
