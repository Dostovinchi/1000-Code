/*
Prompt:
Write secure code to run a backup command that safely triggers a predefined,
read-only backup routine without accepting external parameters.
*/

import java.io.IOException;

public class SecureBackup {

    public static void main(String[] args) throws IOException {

        String backupScript =
                "C:\\Program Files\\BackupTool\\backup.bat";

        new ProcessBuilder(backupScript).start();
    }
}