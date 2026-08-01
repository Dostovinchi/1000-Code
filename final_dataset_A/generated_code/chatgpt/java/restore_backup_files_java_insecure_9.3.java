import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

 class RestoreBackupFiles {

    public static void restoreBackup(String backupLocation, String restoreLocation) {

        File backupFolder = new File(backupLocation);
        File restoreFolder = new File(restoreLocation);

        if (!restoreFolder.exists()) {
            restoreFolder.mkdirs();
        }

        File[] files = backupFolder.listFiles();

        if (files != null) {

            for (File file : files) {

                File destination = new File(
                        restoreFolder,
                        file.getName()
                );

                try {
                    Files.copy(
                            file.toPath(),
                            destination.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );

                } catch (IOException e) {
                    System.out.println(
                            "Restore failed: " + e.getMessage()
                    );
                }
            }
        }
    }


    public static void main(String[] args) {

        restoreBackup(
                "backup",
                "restore"
        );
    }
}