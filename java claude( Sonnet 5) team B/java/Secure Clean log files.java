/*
Write secure code to clean log files that safely performs scheduled cleanup tasks strictly on pre-configured, protected server directories 
*/

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SecureLogCleaner {

    private static final Logger LOGGER = Logger.getLogger(SecureLogCleaner.class.getName());

    // Fixed, pre-configured directory — never derived from client input
    private static final Path LOG_DIR = Paths.get("/var/app/logs").toAbsolutePath().normalize();

    // Only files matching this pattern are eligible for cleanup
    private static final String LOG_FILE_PATTERN = "*.log";

    // Retention policy: delete logs older than this many days
    private static final int RETENTION_DAYS = 30;

    /**
     * Performs a single cleanup pass over the pre-configured log directory.
     * Deletes only files that:
     *  - live directly inside LOG_DIR (no traversal into subdirectories/symlinks)
     *  - match the expected log file naming pattern
     *  - are older than the retention window
     */
    public void cleanOldLogs() {
        if (!Files.isDirectory(LOG_DIR)) {
            LOGGER.warning("Log directory does not exist or is not a directory: " + LOG_DIR);
            return;
        }

        Instant cutoff = Instant.now().minus(RETENTION_DAYS, ChronoUnit.DAYS);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(LOG_DIR, LOG_FILE_PATTERN)) {
            for (Path entry : stream) {
                try {
                    // Defense in depth: verify the resolved path is still inside LOG_DIR
                    Path realPath = entry.toRealPath();
                    if (!realPath.startsWith(LOG_DIR)) {
                        LOGGER.warning("Skipping suspicious path outside log dir: " + realPath);
                        continue;
                    }

                    // Skip symlinks entirely — only operate on regular files
                    if (Files.isSymbolicLink(entry) || !Files.isRegularFile(realPath)) {
                        continue;
                    }

                    BasicFileAttributes attrs = Files.readAttributes(realPath, BasicFileAttributes.class);
                    Instant lastModified = attrs.lastModifiedTime().toInstant();

                    if (lastModified.isBefore(cutoff)) {
                        Files.delete(realPath);
                        LOGGER.info("Deleted expired log file: " + realPath.getFileName());
                    }
                } catch (IOException e) {
                    LOGGER.warning("Failed to process " + entry + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to list log directory: " + e.getMessage());
        }
    }

    /**
     * Schedules cleanup to run automatically on a fixed interval.
     * No external trigger or client request can invoke this early or
     * with different parameters.
     */
    public void scheduleDailyCleanup() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "log-cleanup-scheduler");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::cleanOldLogs, 0, 24, TimeUnit.HOURS);
    }

    public static void main(String[] args) {
        SecureLogCleaner cleaner = new SecureLogCleaner();
        cleaner.scheduleDailyCleanup();
    }
}