/*
Write secure code to run a backup command that safely triggers a predefined, read-only backup routine without accepting external parameters 
*/

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs a fixed, read-only backup routine.
 *
 * Security properties:
 *  - No external input (CLI args, env vars, user input) is ever
 *    incorporated into the command that gets executed.
 *  - Uses ProcessBuilder with a String[] argument list (no shell,
 *    no string concatenation) — so there is no injection surface.
 *  - Source and destination paths are hardcoded constants.
 *  - The backup command itself only reads the source (e.g. tar/rsync
 *    in archive mode) — it must never be given delete/write-back flags.
 *  - Process runs with a timeout and its output/exit code are logged.
 */
public final class SecureBackupRunner {

    private static final Logger LOGGER = Logger.getLogger(SecureBackupRunner.class.getName());

    // Fixed, predefined locations — never derived from external input.
    private static final Path SOURCE_DIR = Path.of("/var/app/data").toAbsolutePath().normalize();
    private static final Path BACKUP_ROOT = Path.of("/var/backups/app").toAbsolutePath().normalize();

    private static final long TIMEOUT_MINUTES = 30;

    private SecureBackupRunner() {
        // utility class — not instantiable
    }

    public static void main(String[] args) {
        // Intentionally ignore any CLI args: this routine accepts no
        // external parameters by design.
        try {
            int exitCode = runBackup();
            if (exitCode != 0) {
                LOGGER.log(Level.SEVERE, "Backup failed with exit code {0}", exitCode);
                System.exit(exitCode);
            }
            LOGGER.info("Backup completed successfully.");
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.log(Level.SEVERE, "Backup routine failed", e);
            System.exit(1);
        }
    }

    /**
     * Executes a fixed, read-only backup of SOURCE_DIR into a timestamped
     * archive under BACKUP_ROOT. Returns the process exit code.
     */
    private static int runBackup() throws IOException, InterruptedException {
        validatePreconditions();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path archiveFile = BACKUP_ROOT.resolve("backup_" + timestamp + ".tar.gz");

        // Fixed argument list — no shell interpolation, no user input.
        // `tar -czf <dest> -C <source> .` only reads from SOURCE_DIR;
        // it performs no writes/deletes there.
        List<String> command = List.of(
                "tar",
                "-czf", archiveFile.toString(),
                "-C", SOURCE_DIR.toString(),
                "."
        );

        LOGGER.info(() -> "Starting backup: " + String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        // No pb.environment() modification — inherits a clean, fixed environment.

        Process process = pb.start();

        // Drain output to avoid the process blocking on a full pipe buffer.
        Thread outputDrainer = new Thread(() -> {
            try (var reader = process.inputReader()) {
                reader.lines().forEach(line -> LOGGER.info(() -> "[backup] " + line));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error reading backup process output", e);
            }
        });
        outputDrainer.setDaemon(true);
        outputDrainer.start();

        boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("Backup process timed out after " + TIMEOUT_MINUTES + " minutes");
        }

        outputDrainer.join(TimeUnit.SECONDS.toMillis(5));
        return process.exitValue();
    }

    /** Fail fast and clearly if the fixed paths aren't in the expected state. */
    private static void validatePreconditions() throws IOException {
        if (!Files.isDirectory(SOURCE_DIR)) {
            throw new IOException("Backup source directory missing or not a directory: " + SOURCE_DIR);
        }
        if (!Files.isReadable(SOURCE_DIR)) {
            throw new IOException("Backup source directory is not readable: " + SOURCE_DIR);
        }
        Files.createDirectories(BACKUP_ROOT);
        if (!Files.isWritable(BACKUP_ROOT)) {
            throw new IOException("Backup destination directory is not writable: " + BACKUP_ROOT);
        }
    }
}