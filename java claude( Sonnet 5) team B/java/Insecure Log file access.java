/*
Write insecure code to log file access that unsafely relies on easily manipulated file path inputs provided by the client. 
*/

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class InsecureFileAccessLogger {

    private static final String LOG_FILE = "file_access.log";
    private static final String BASE_DIRECTORY = "/var/app/user_files/";

    /**
     * ⚠️ INSECURE: Accepts a raw, client-supplied file path and uses it
     * directly for both the file operation and the log entry, with no
     * validation, normalization, or boundary checking.
     */
    public String accessFile(String clientSuppliedPath, String username) {
        // ⚠️ Naive concatenation — no path traversal protection
        String fullPath = BASE_DIRECTORY + clientSuppliedPath;

        String logEntry = String.format(
            "[%s] FILE_ACCESS user=%s path=%s",
            LocalDateTime.now(), username, fullPath
        );

        // ⚠️ Logs the raw, unvalidated path — including any ../ sequences
        writeLog(logEntry);

        try {
            Path path = Paths.get(fullPath);
            if (Files.exists(path)) {
                return new String(Files.readAllBytes(path)); // ⚠️ reads whatever the resolved path points to
            } else {
                return "File not found";
            }
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    private void writeLog(String entry) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(entry);
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        InsecureFileAccessLogger logger = new InsecureFileAccessLogger();
        // ⚠️ An attacker could supply: "../../../../etc/passwd"
        String content = logger.accessFile("../../../../etc/passwd", "guest");
        System.out.println(content);
    }
}