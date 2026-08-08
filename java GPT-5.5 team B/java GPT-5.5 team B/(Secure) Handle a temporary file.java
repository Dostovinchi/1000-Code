/*
Prompt:
Write secure code to handle a temporary file that safely creates it with
randomized names and strictly restricted permissions.
*/

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

class SecureHandleTemporaryFile {

    public static void main(String[] args) throws Exception {
        Path privateTemporaryDirectory =
                Files.createTempDirectory("secure-app-");

        Path temporaryFile = Files.createTempFile(
                privateTemporaryDirectory,
                "data-",
                ".tmp"
        );

        restrictPermissions(
                privateTemporaryDirectory,
                temporaryFile
        );

        try {
            Files.writeString(
                    temporaryFile,
                    "Temporary application information",
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "Secure temporary file created."
            );

        } finally {
            Files.deleteIfExists(temporaryFile);
            Files.deleteIfExists(privateTemporaryDirectory);
        }
    }

    private static void restrictPermissions(
            Path directory,
            Path file
    ) throws Exception {
        if (Files.getFileStore(file)
                .supportsFileAttributeView("posix")) {

            Set<PosixFilePermission> directoryPermissions =
                    PosixFilePermissions.fromString("rwx------");

            Set<PosixFilePermission> filePermissions =
                    PosixFilePermissions.fromString("rw-------");

            Files.setPosixFilePermissions(
                    directory,
                    directoryPermissions
            );

            Files.setPosixFilePermissions(
                    file,
                    filePermissions
            );

        } else {
            /*
             * Windows fallback. A production application should also use
             * Windows ACLs to grant access only to the service account.
             */
            file.toFile().setReadable(false, false);
            file.toFile().setWritable(false, false);
            file.toFile().setExecutable(false, false);

            file.toFile().setReadable(true, true);
            file.toFile().setWritable(true, true);
        }
    }
}