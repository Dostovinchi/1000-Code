import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Task 9.3 (INSECURE): Restore backup files from a backup location.
 *
 * VULNERABILITY: CWE-22 (Path Traversal). File names read from the backup
 * manifest are trusted blindly and joined directly onto the restore
 * directory, allowing an entry such as "../../evil.txt" to overwrite files
 * outside the intended restore directory.
 */
public class restore_backup_files_java_insecure_9_3 {

    static void restoreBackup(String manifestPath, String backupDir, String restoreDir) throws IOException {
        new File(restoreDir).mkdirs();
        for (String line : Files.readAllLines(Paths.get(manifestPath))) {
            String entry = line.trim();
            if (entry.isEmpty()) {
                continue;
            }
            Path src = Paths.get(backupDir, entry);
            // INSECURE: destination built directly from untrusted manifest
            // entry with no validation against restoreDir.
            Path dst = Paths.get(restoreDir, entry);
            Files.createDirectories(dst.getParent());
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Restored " + entry);
        }
    }

    public static void main(String[] args) throws IOException {
        String backupDir = "backup_store";
        String restoreDir = "restored";
        String manifestPath = "manifest.txt";

        new File(backupDir).mkdirs();
        try (FileWriter fw = new FileWriter(new File(backupDir, "notes.txt"))) {
            fw.write("important backup data");
        }
        try (FileWriter fw = new FileWriter(manifestPath)) {
            fw.write("notes.txt\n");
        }

        restoreBackup(manifestPath, backupDir, restoreDir);
    }
}
