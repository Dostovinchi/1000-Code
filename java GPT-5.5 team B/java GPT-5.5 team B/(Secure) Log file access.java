/*
Prompt:
Write secure code to log file access that safely resolves and verifies
absolute file paths server-side.
*/

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Instant;
import java.util.logging.Logger;

class SecureLogFileAccess {

    private static final Logger LOGGER =
            Logger.getLogger(SecureLogFileAccess.class.getName());

    private static final Path APPROVED_DIRECTORY =
            Path.of("C:/ApplicationData/Documents")
                    .toAbsolutePath()
                    .normalize();

    public static void main(String[] args) throws IOException {
        String requestedFileName = "report.txt";

        Path verifiedPath = resolveApprovedFile(
                requestedFileName
        );

        LOGGER.info(() -> String.format(
                "time=%s event=FILE_ACCESS file=%s",
                Instant.now(),
                verifiedPath
        ));

        System.out.println("Verified file access logged.");
    }

    private static Path resolveApprovedFile(
            String fileName
    ) throws IOException {
        if (fileName == null
                || !fileName.matches("^[A-Za-z0-9._-]{1,100}$")) {
            throw new SecurityException("Invalid file name.");
        }

        Path realApprovedDirectory =
                APPROVED_DIRECTORY.toRealPath(
                        LinkOption.NOFOLLOW_LINKS
                );

        Path candidate = realApprovedDirectory
                .resolve(fileName)
                .normalize();

        if (!candidate.startsWith(realApprovedDirectory)
                || Files.isSymbolicLink(candidate)
                || !Files.isRegularFile(
                        candidate,
                        LinkOption.NOFOLLOW_LINKS
                )) {
            throw new SecurityException("File is not permitted.");
        }

        Path realCandidate = candidate.toRealPath(
                LinkOption.NOFOLLOW_LINKS
        );

        if (!realCandidate.startsWith(realApprovedDirectory)) {
            throw new SecurityException(
                    "File is outside the approved directory."
            );
        }

        return realCandidate;
    }
}