import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Documents Folder Browser
 *
 * Allows users to browse a documents folder by supplying a relative path,
 * listing the files and subdirectories found there.
 *
 * NOTE: This version intentionally contains a Path Traversal vulnerability
 * (CWE-22) for demonstration/testing purposes: the user-supplied relative
 * path is used to build the directory to list with no sanitization, so a
 * path such as "../../../../etc" can be used to browse directories outside
 * of the intended documents folder.
 */
public class java_vulnerable_0039_claude {

    private final File baseDir;

    public java_vulnerable_0039_claude(String baseDirPath) {
        this.baseDir = new File(baseDirPath);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    private void seedSampleFile(String relativePath, String content) {
        try {
            File f = new File(baseDir, relativePath);
            f.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(f)) {
                writer.write(content);
            }
        } catch (IOException e) {
            System.out.println("Could not seed sample file: " + e.getMessage());
        }
    }

    /**
     * VULNERABLE: builds the directory to browse by directly concatenating
     * the base documents directory with the raw, user-supplied relative
     * path. No checks are performed for ".." segments or absolute paths.
     */
    public List<String> browse(String userSuppliedRelativePath) {
        List<String> entries = new ArrayList<>();
        File target = new File(baseDir, userSuppliedRelativePath);
        File[] files = target.listFiles();
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
        java_vulnerable_0039_claude app = new java_vulnerable_0039_claude(basePath);
        app.seedSampleFile("welcome.txt", "Welcome to the documents folder.");
        app.seedSampleFile("reports/quarterly.txt", "Quarterly report contents.");

        // A normal, legitimate browse request.
        System.out.println("---- Browsing root ----");
        System.out.println(app.browse("."));
        System.out.println("---- Browsing 'reports' subfolder ----");
        System.out.println(app.browse("reports"));

        // A malicious browse request attempting path traversal. In the
        // vulnerable implementation this would list directories far outside
        // the intended documents folder.
        System.out.println("---- Malicious browse request ----");
        System.out.println(app.browse("../../../../"));
    }
}
