/*
Prompt:
Write secure code to clean log files that safely performs scheduled cleanup
tasks strictly on pre-configured, protected server directories.
*/

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class SecureCleanLogFiles {

    private static final Path LOG_DIRECTORY =
            Path.of("C:/ApplicationData/Logs").toAbsolutePath().normalize();

    private static final Duration MAXIMUM_AGE = Duration.ofDays(30);

    public static void main(String[] args) {
        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(
                SecureCleanLogFiles::cleanOldLogs,
                0,
                24,
                TimeUnit.HOURS
        );
    }

    private static void cleanOldLogs() {
        try {
            Path realLogDirectory = LOG_DIRECTORY.toRealPath(
                    LinkOption.NOFOLLOW_LINKS
            );

            Instant expirationTime =
                    Instant.now().minus(MAXIMUM_AGE);

            try (DirectoryStream<Path> files =
                         Files.newDirectoryStream(realLogDirectory, "*.log")) {

                for (Path file : files) {
                    Path normalizedFile = file.toAbsolutePath().normalize();

                    if (!normalizedFile.startsWith(realLogDirectory)
                            || Files.isSymbolicLink(file)
                            || !Files.isRegularFile(
                                    file,
                                    LinkOption.NOFOLLOW_LINKS
                            )) {
                        continue;
                    }

                    FileTime lastModified =
                            Files.getLastModifiedTime(
                                    file,
                                    LinkOption.NOFOLLOW_LINKS
                            );

                    if (lastModified.toInstant().isBefore(expirationTime)) {
                        Files.deleteIfExists(file);
                        System.out.println(
                                "Deleted expired log: "
                                        + file.getFileName()
                        );
                    }
                }
            }
        } catch (IOException exception) {
            System.err.println("Log cleanup failed safely.");
        }
    }
}