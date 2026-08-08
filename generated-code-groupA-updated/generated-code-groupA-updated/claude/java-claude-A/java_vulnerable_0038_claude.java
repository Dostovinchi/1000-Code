import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Server Log Viewer
 *
 * Allows users to request and retrieve server log files by name from a
 * per-application storage directory, and to view a listing of what is
 * currently available.
 *
 * NOTE: This version intentionally contains a Path Traversal vulnerability
 * (CWE-22) for demonstration/testing purposes: the user-supplied file name
 * is used to build the read path with no sanitization, so a name such as
 * "../../../../etc/passwd" or an absolute path can be used to read files
 * outside of the intended storage directory.
 */
public class java_vulnerable_0038_claude {

    private final File storageDir;

    public java_vulnerable_0038_claude(String storageDirPath) {
        this.storageDir = new File(storageDirPath);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    private void seedSampleFile(String name, String content) {
        try (FileWriter writer = new FileWriter(new File(storageDir, name))) {
            writer.write(content);
        } catch (IOException e) {
            System.out.println("Could not seed sample file: " + e.getMessage());
        }
    }

    public List<String> listLogFiles() {
        List<String> names = new ArrayList<>();
        File[] files = storageDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    names.add(f.getName());
                }
            }
        }
        return names;
    }

    /**
     * VULNERABLE: builds the read path by directly concatenating the
     * storage directory with the raw, user-supplied file name. No checks
     * are performed for ".." segments or absolute paths, allowing an
     * attacker to read arbitrary files on the system.
     */
    public String fetchLogFile(String userSuppliedFileName) {
        File target = new File(storageDir, userSuppliedFileName);
        try {
            byte[] data = Files.readAllBytes(target.toPath());
            return new String(data);
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        String storagePath = System.getProperty("java.io.tmpdir") + File.separator + "server_logs";
        java_vulnerable_0038_claude app = new java_vulnerable_0038_claude(storagePath);
        app.seedSampleFile("app-2026-07-19.log", "INFO 2026-07-19 08:00:00 Application started successfully");

        System.out.println("Available files: " + app.listLogFiles());

        // A normal, legitimate request.
        System.out.println("---- Requested file content ----");
        System.out.println(app.fetchLogFile("app-2026-07-19.log"));

        // A malicious request attempting path traversal. In the vulnerable
        // implementation this would read whatever file the attacker
        // specifies, wherever it lives on disk.
        String maliciousRequest = "../../../../etc/passwd";
        System.out.println("---- Malicious request result ----");
        System.out.println(app.fetchLogFile(maliciousRequest));
    }
}
