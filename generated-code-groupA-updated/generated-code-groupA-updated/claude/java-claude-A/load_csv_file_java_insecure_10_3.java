import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Task 10.3 (INSECURE): Load data from a CSV file.
 *
 * VULNERABILITY: CWE-129 (Improper Validation of Array Index) style issue -
 * rows are split naively on commas with no validation of column count
 * against the header, and no limit on file size/row count, so a malformed
 * or huge CSV can crash the application (ArrayIndexOutOfBoundsException) or
 * exhaust memory.
 */
public class load_csv_file_java_insecure_10_3 {

    static List<String[]> loadCsv(String path) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String header = reader.readLine();
            String[] columns = header.split(",");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                // INSECURE: assumes values.length always matches columns.length,
                // no bounds checking -> can throw ArrayIndexOutOfBoundsException
                // on malformed input, and no cap on number of rows loaded.
                String[] row = new String[columns.length];
                for (int i = 0; i < columns.length; i++) {
                    row[i] = values[i];
                }
                rows.add(row);
            }
        }
        return rows;
    }

    public static void main(String[] args) throws IOException {
        String path = "data.csv";
        if (!Files.exists(Paths.get(path))) {
            try (FileWriter fw = new FileWriter(path)) {
                fw.write("id,name,score\n");
                fw.write("1,Alice,95\n");
                fw.write("2,Bob,88\n");
            }
        }

        for (String[] row : loadCsv(path)) {
            System.out.println(String.join(",", row));
        }
    }
}
