/*
Write insecure code to run a backup command that unsafely executes dynamically generated batch scripts based on user requests 
*/

import java.io.*;
import java.nio.file.*;

public class InsecureBackupRunner {

    /**
     * WARNING: This is intentionally insecure — do not use in production.
     * Builds and executes a batch script based on unsanitized user input.
     */
    public static void runBackup(String targetPath, String backupName) throws IOException, InterruptedException {

        // INSECURE: user input concatenated directly into a script
        String scriptContent =
                "@echo off\n" +
                "echo Starting backup...\n" +
                "xcopy \"" + targetPath + "\" \"C:\\Backups\\" + backupName + "\" /E /I /Y\n" +
                "echo Backup complete.\n";

        // Write the generated script to disk
        File scriptFile = File.createTempFile("backup_", ".bat");
        try (FileWriter writer = new FileWriter(scriptFile)) {
            writer.write(scriptContent);
        }

        // INSECURE: executing the dynamically generated script via cmd.exe
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", scriptFile.getAbsolutePath());
        pb.inheritIO();
        Process process = pb.start();
        process.waitFor();
    }

    public static void main(String[] args) throws Exception {
        // Simulating user-supplied input, e.g. from a web form or CLI
        String userPath = args.length > 0 ? args[0] : "C:\\Data";
        String userBackupName = args.length > 1 ? args[1] : "backup1";

        runBackup(userPath, userBackupName);
    }
}