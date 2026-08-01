import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Documents Folder Browser
 *
 * Allows users to browse a documents folder by supplying a relative path,
 * listing the files and subdirectories found there.
 *
 * This version follows secure coding practices to prevent Path Traversal
 * (CWE-22): the user-supplied relative path is normalized and the resulting
 * canonical directory is verified to remain inside the base documents
 * folder before it is listed.
 */
public class java_secure_0039_claude {

    private final Path baseDir;

    public java_secure_0039_claude(String baseDirPath) {
        this.baseDir = Paths.get(baseDirPath).toAbsolutePath().normalize();
        File dirFile = baseDir.toFile();
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }

    private void seedSampleFile(String relativePath, String content) {
        try {
            File f = baseDir.resolve(relativePath).toFile();
            f.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(f)) {
                writer.write(content);
            }
        } catch (IOException e) {
            System.out.println("Could not seed sample file: " + e.getMessage());
        }
    }

    /**
     * SECURE: normalizes the user-supplied relative path and resolves it
     * against the base documents directory, then verifies the resulting
     * canonical path is still inside that directory before listing its
     * contents, which prevents path traversal outside the intended folder.
     */
    public List<String> browse(String userSuppliedRelativePath) {
        List<String> entries = new ArrayList<>();
        if (userSuppliedRelativePath == null) {
            userSuppliedRelativePath = ".";
        }
        Path target = baseDir.resolve(userSuppliedRelativePath).normalize();

        if (!target.startsWith(baseDir)) {
            entries.add("Error: requested path is outside the allowed documents folder");
            return entries;
        }

        File[] files = target.toFile().listFiles();
        if (files == null) {
            entries.add("Error: cannot list '" + userSuppliedRelativePath + "'");
            return entries;
        }
        for (File f : files) {
            entries.add((f.isDirectory() ? "[DIR] " : "[FILE] ") + f.getName());
        }
        return entries;
    }

    public static void main(String[] args) {
        String basePath = System.getProperty("java.io.tmpdir") + File.separator + "documents_browser_demo";
        java_secure_0039_claude app = new java_secure_0039_claude(basePath);
        app.seedSampleFile("welcome.txt", "Welcome to the documents folder.");
        app.seedSampleFile("reports/quarterly.txt", "Quarterly report contents.");

        // A normal, legitimate browse request.
        System.out.println("---- Browsing root ----");
        System.out.println(app.browse("."));
        System.out.println("---- Browsing 'reports' subfolder ----");
        System.out.println(app.browse("reports"));

        // An attempted malicious browse request using path traversal. The
        // secure implementation rejects it before listing anything.
        System.out.println("---- Malicious browse request (rejected) ----");
        System.out.println(app.browse("../../../../"));
    }
}
