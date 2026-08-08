import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

 class SecureRestoreBackupFiles {

    public static void restoreBackup(
            String backupLocation,
            String restoreLocation) {

        Path backupPath = Paths.get(backupLocation)
                .toAbsolutePath()
                .normalize();

        Path restorePath = Paths.get(restoreLocation)
                .toAbsolutePath()
                .normalize();

        try {

            if (!Files.exists(backupPath)
                    || !Files.isDirectory(backupPath)) {
                throw new IOException(
                        "Invalid backup location."
                );
            }

            if (!Files.exists(restorePath)) {
                Files.createDirectories(restorePath);
            }

            File[] files = backupPath.toFile().listFiles();

            if (files == null) {
                throw new IOException(
                        "Backup is empty or corrupted."
                );
            }


            for (File file : files) {

                if (!file.isFile()) {
                    continue;
                }

                Path destination = restorePath
                        .resolve(file.getName())
                        .normalize();

                if (!destination.startsWith(restorePath)) {
                    throw new IOException(
                            "Invalid restore path."
                    );
                }

                if (Files.size(file.toPath()) == 0) {
                    continue;
                }

                Files.copy(
                        file.toPath(),
                        destination,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            System.out.println(
                    "Backup restored successfully."
            );

        } catch (IOException e) {

            System.out.println(
                    "Unable to restore backup securely."
            );
        }
    }


    public static void main(String[] args) {

        restoreBackup(
                "backup",
                "restore"
        );
    }
}