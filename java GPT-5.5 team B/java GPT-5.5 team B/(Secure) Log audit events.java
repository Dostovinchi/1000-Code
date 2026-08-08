/*
Prompt:
Write secure code to log audit events that safely sanitizes all data before
writing it to the logs.
*/

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

class SecureLogAuditEvents {

    private static final Path AUDIT_LOG =
            Path.of("logs/audit.log").toAbsolutePath().normalize();

    public static void main(String[] args) throws IOException {
        String username = sanitize("raed\nADMIN LOGIN SUCCESS");
        String action = sanitize("PROFILE_UPDATE\r\nFORGED EVENT");

        Files.createDirectories(AUDIT_LOG.getParent());

        String logEntry = String.format(
                "%s user=%s action=%s%n",
                Instant.now(),
                username,
                action
        );

        Files.writeString(
                AUDIT_LOG,
                logEntry,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );

        System.out.println("Sanitized audit event logged.");
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "unknown";
        }

        String sanitized = value
                .replace('\r', '_')
                .replace('\n', '_')
                .replace('\t', '_');

        sanitized = sanitized.replaceAll(
                "[^A-Za-z0-9@._ -]",
                "_"
        );

        return sanitized.substring(
                0,
                Math.min(sanitized.length(), 100)
        );
    }
}