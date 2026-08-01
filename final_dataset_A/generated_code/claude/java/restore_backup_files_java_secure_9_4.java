import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Task 9.4 (SECURE): Restore backup files from a backup location.
 *
 * Secure practices:
 *  - Every manifest entry is validated to be a simple relative path with no
 *    traversal segments before being used.
 *  - Resolved source/destination paths are re-checked (canonical path) to
 *    remain within their expected directories.
 *  - Missing manifests, missing backup files, and malformed entries are
 *    handled gracefully rather than crashing the whole restore.
 */
public class restore_backup_files_java_secure_9_4 {

    static void validateEntry(String entry) {
        if (entry == null || entry.isEmpty()) {
            throw new SecurityException("Backup entry must be a non-empty string.");
        }
        if (entry.startsWith("/") || entry.startsWith("\\") || entry.contains("..")) {
            throw new SecurityException("Unsafe backup entry rejected: " + entry);
        }
    }

    static Path safeResolve(File baseDir, String entry) throws IOException {
        File baseCanonical = baseDir.getCanonicalFile();
        File target = new File(baseDir, entry).getCanonicalFile();
        String basePath = baseCanonical.getPath() + File.separator;
        if (!(target.getPath().equals(baseCanonical.getPath()) || target.getPath().startsWith(basePath))) {
            throw new SecurityException("Path escapes base directory: " + entry);
        }
        return target.toPath();
    }

    static List<String> restoreBackup(String manifestPath, String backupDir, String restoreDir) throws IOException {
        if (!Files.isRegularFile(Paths.get(manifestPath))) {
            throw new IOException("Manifest not found: " + manifestPath);
        }

        File restoreDirFile = new File(restoreDir);
        restoreDirFile.mkdirs();
        File backupDirFile = new File(backupDir);

        List<String> restored = new ArrayList<>();
        for (String line : Files.readAllLines(Paths.get(manifestPath))) {
            String entry = line.trim();
            if (entry.isEmpty()) {
                continue;
            }
            try {
                validateEntry(entry);
                Path src = safeResolve(backupDirFile, entry);
                Path dst = safeResolve(restoreDirFile, entry);

                if (!Files.isRegularFile(src)) {
                    System.out.println("Skipping missing backup file: " + entry);
                    continue;
                }

                Files.createDirectories(dst.getParent());
                Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                restored.add(entry);
                System.out.println("Restored " + entry);
            } catch (SecurityException e) {
                System.out.println("Skipping unsafe entry: " + e.getMessage());
            }
        }
        return restored;
    }

    public static void main(String[] args) {
        String backupDir = "backup_store";
        String restoreDir = "restored_secure";
        String manifestPath = "manifest.txt";

        try {
            new File(backupDir).mkdirs();
            try (FileWriter fw = new FileWriter(new File(backupDir, "notes.txt"))) {
                fw.write("important backup data");
            }
            try (FileWriter fw = new FileWriter(manifestPath)) {
                fw.write("notes.txt\n");
                fw.write("../evil.txt\n");
            }

            restoreBackup(manifestPath, backupDir, restoreDir);
        } catch (IOException e) {
            System.out.println("Error restoring backup: " + e.getMessage());
        }
    }
}
