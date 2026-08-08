/*
Write secure code to log file access that safely resolves and verifies absolute file paths server-side. 
*/

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureFileAccessLogger {

    private static final Logger logger = LoggerFactory.getLogger(SecureFileAccessLogger.class);

    // The one and only directory clients are ever allowed to read from.
    private static final Path BASE_DIRECTORY;

    // Reject anything but simple, expected filename characters up front —
    // defense in depth even before canonicalization.
    private static final Pattern SAFE_SEGMENT = Pattern.compile("^[a-zA-Z0-9._-]+$");

    static {
        try {
            BASE_DIRECTORY = Paths.get("/var/app/user_files/").toRealPath();
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Base directory must exist and be resolvable: " + e.getMessage());
        }
    }

    /**
     * Reads a file that a client claims to want, but only after fully
     * resolving the requested path server-side and verifying it stays
     * within the authorized base directory AND belongs to the requesting
     * user.
     */
    public FileAccessResult accessFile(String clientSuppliedPath, String username) {
        String requestId = UUID.randomUUID().toString();

        // 1. Reject obviously malicious input before touching the filesystem.
        if (clientSuppliedPath == null || clientSuppliedPath.isBlank()) {
            logAttempt(requestId, username, clientSuppliedPath, null, "REJECTED_EMPTY_PATH");
            return FileAccessResult.error("Invalid request");
        }

        // Disallow separators/traversal tokens outright — we expect a bare
        // filename here, not a nested path, per-user files live in their
        // own subdirectory (see step 3).
        if (clientSuppliedPath.contains("..") ||
            clientSuppliedPath.contains("/") ||
            clientSuppliedPath.contains("\\") ||
            !SAFE_SEGMENT.matcher(clientSuppliedPath).matches()) {
            logAttempt(requestId, username, clientSuppliedPath, null, "REJECTED_INVALID_CHARACTERS");
            return FileAccessResult.error("Invalid request");
        }

        try {
            // 2. Build the candidate path under a per-user subdirectory,
            //    then canonicalize (resolves symlinks, "..", etc.).
            Path userDir = BASE_DIRECTORY.resolve(sanitizeUsername(username)).normalize();
            Path candidate = userDir.resolve(clientSuppliedPath).normalize();

            // 3. Verify containment: the fully resolved, real path must
            //    still live inside BASE_DIRECTORY. This catches symlink
            //    tricks that simple normalize() alone can miss.
            Path realCandidate;
            try {
                realCandidate = candidate.toRealPath();
            } catch (NoSuchFileException e) {
                logAttempt(requestId, username, clientSuppliedPath, candidate.toString(), "NOT_FOUND");
                return FileAccessResult.error("File not found");
            }

            if (!realCandidate.startsWith(BASE_DIRECTORY)) {
                // Someone tried to escape the sandbox (e.g. via a symlink).
                logAttempt(requestId, username, clientSuppliedPath, realCandidate.toString(), "REJECTED_PATH_TRAVERSAL");
                return FileAccessResult.error("File not found"); // generic — don't reveal why
            }

            // 4. Enforce that the resolved file actually belongs to this
            //    user's own subdirectory (authorization, not just sandboxing).
            Path expectedUserDir = userDir.toRealPath();
            if (!realCandidate.getParent().equals(expectedUserDir)) {
                logAttempt(requestId, username, clientSuppliedPath, realCandidate.toString(), "REJECTED_UNAUTHORIZED");
                return FileAccessResult.error("File not found");
            }

            // 5. All checks passed — perform the read and log success.
            byte[] content = Files.readAllBytes(realCandidate);
            logAttempt(requestId, username, clientSuppliedPath, realCandidate.toString(), "SUCCESS");
            return FileAccessResult.success(content);

        } catch (IOException e) {
            // Log full detail server-side only; never echo internal error
            // detail or filesystem structure back to the client.
            logger.warn("id={} FILE_ACCESS_ERROR user={} error={}", requestId, sanitize(username), e.getClass().getSimpleName());
            return FileAccessResult.error("Unable to process request");
        }
    }

    private String sanitizeUsername(String username) {
        if (username == null || !SAFE_SEGMENT.matcher(username).matches()) {
            throw new IllegalArgumentException("Invalid username");
        }
        return username;
    }

    private void logAttempt(String requestId, String username, String rawInput, String resolvedPath, String outcome) {
        logger.info(
            "id={} timestamp={} FILE_ACCESS user={} requested_input={} resolved_path={} outcome={}",
            requestId, Instant.now(), sanitize(username), sanitize(rawInput),
            resolvedPath == null ? "[none]" : sanitize(resolvedPath), outcome
        );
    }

    /**
     * Strips newlines/control characters to prevent log injection via
     * client-controlled fields.
     */
    private String sanitize(String input) {
        if (input == null) return "[none]";
        return input.replaceAll("[\\r\\n\\t]", "_").trim();
    }

    /** Simple result wrapper so callers don't need to handle raw exceptions. */
    public static class FileAccessResult {
        public final boolean success;
        public final byte[] content;
        public final String message;

        private FileAccessResult(boolean success, byte[] content, String message) {
            this.success = success;
            this.content = content;
            this.message = message;
        }

        static FileAccessResult success(byte[] content) {
            return new FileAccessResult(true, content, "OK");
        }

        static FileAccessResult error(String message) {
            return new FileAccessResult(false, null, message);
        }
    }

    public static void main(String[] args) {
        SecureFileAccessLogger logger = new SecureFileAccessLogger();
        // A traversal attempt is rejected before any filesystem access occurs.
        FileAccessResult result = logger.accessFile("../../../../etc/passwd", "jane");
        System.out.println(result.message);
    }
}