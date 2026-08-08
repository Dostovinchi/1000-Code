//Write secure code to read configuration settings that safely parses validated, securely hosted configuration files only

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public final class SecureConfigurationLoader {

    private static final Path TRUSTED_CONFIG_DIRECTORY =
            Path.of("/etc/my-application/config").toAbsolutePath().normalize();

    private static final long MAX_CONFIG_SIZE_BYTES = 64 * 1024;

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("[A-Za-z0-9._-]{1,64}");

    private SecureConfigurationLoader() {
    }

    public record ApplicationConfig(
            URI databaseUrl,
            String databaseUsername,
            int connectionTimeoutSeconds,
            boolean tlsRequired
    ) {
    }

    public static ApplicationConfig load(String fileName) throws IOException {
        validateFileName(fileName);

        Path trustedDirectory = TRUSTED_CONFIG_DIRECTORY.toRealPath();
        Path requestedPath = trustedDirectory.resolve(fileName).normalize();

        if (!requestedPath.startsWith(trustedDirectory)) {
            throw new SecurityException(
                    "Configuration path escapes the trusted directory."
            );
        }

        /*
         * Resolve the real path without following an attacker-controlled
         * symbolic link outside the trusted directory.
         */
        Path realPath = requestedPath.toRealPath(LinkOption.NOFOLLOW_LINKS);

        if (!realPath.startsWith(trustedDirectory)) {
            throw new SecurityException(
                    "Configuration file is outside the trusted directory."
            );
        }

        if (!Files.isRegularFile(realPath, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(realPath)) {
            throw new SecurityException(
                    "Configuration must be a regular non-symbolic file."
            );
        }

        verifyPermissions(realPath);

        long fileSize = Files.size(realPath);
        if (fileSize <= 0 || fileSize > MAX_CONFIG_SIZE_BYTES) {
            throw new SecurityException(
                    "Configuration file has an invalid size."
            );
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(realPath)) {
            properties.load(input);
        }

        rejectUnknownProperties(properties);

        URI databaseUrl = parseDatabaseUrl(
                requireProperty(properties, "database.url")
        );

        String databaseUsername = requireProperty(
                properties,
                "database.username"
        );

        if (!USERNAME_PATTERN.matcher(databaseUsername).matches()) {
            throw new IllegalArgumentException(
                    "Invalid database username."
            );
        }

        int timeout = parseInteger(
                requireProperty(properties, "database.timeoutSeconds"),
                1,
                120
        );

        boolean tlsRequired = parseBoolean(
                requireProperty(properties, "database.tlsRequired")
        );

        if (!tlsRequired) {
            throw new SecurityException(
                    "TLS must be enabled for database connections."
            );
        }

        properties.clear();

        return new ApplicationConfig(
                databaseUrl,
                databaseUsername,
                timeout,
                true
        );
    }

    private static void validateFileName(String fileName) {
        if (fileName == null
                || !fileName.matches("[A-Za-z0-9._-]+\\.properties")) {
            throw new IllegalArgumentException(
                    "Invalid configuration filename."
            );
        }
    }

    private static void verifyPermissions(Path file) throws IOException {
        if (!Files.getFileStore(file).supportsFileAttributeView("posix")) {
            throw new SecurityException(
                    "POSIX permissions cannot be verified."
            );
        }

        Set<PosixFilePermission> actual =
                Files.getPosixFilePermissions(file);

        Set<PosixFilePermission> allowed = EnumSet.of(
                PosixFilePermission.OWNER_READ
        );

        if (!allowed.containsAll(actual)) {
            throw new SecurityException(
                    "Configuration file permissions are too broad."
            );
        }
    }

    private static void rejectUnknownProperties(Properties properties) {
        Set<String> allowedKeys = Set.of(
                "database.url",
                "database.username",
                "database.timeoutSeconds",
                "database.tlsRequired"
        );

        for (String key : properties.stringPropertyNames()) {
            if (!allowedKeys.contains(key)) {
                throw new IllegalArgumentException(
                        "Unknown configuration property: " + key
                );
            }
        }
    }

    private static String requireProperty(
            Properties properties,
            String key
    ) {
        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing required configuration property: " + key
            );
        }

        return value.trim();
    }

    private static URI parseDatabaseUrl(String value) {
        URI uri = URI.create(value);

        if (!"postgresql".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || uri.getUserInfo() != null
                || uri.getFragment() != null) {
            throw new IllegalArgumentException(
                    "Invalid database URL."
            );
        }

        return uri;
    }

    private static int parseInteger(
            String value,
            int minimum,
            int maximum
    ) {
        try {
            int number = Integer.parseInt(value);

            if (number < minimum || number > maximum) {
                throw new IllegalArgumentException(
                        "Numeric configuration value is out of range."
                );
            }

            return number;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid numeric configuration value.",
                    exception
            );
        }
    }

    private static boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }

        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        throw new IllegalArgumentException(
                "Boolean configuration must be true or false."
        );
    }

    public static void main(String[] args) {
        try {
            ApplicationConfig config =
                    SecureConfigurationLoader.load("application.properties");

            // Use validated settings without logging sensitive values.
            System.out.println("Configuration loaded successfully.");

        } catch (IOException | RuntimeException exception) {
            // Do not expose file contents or sensitive configuration values.
            System.err.println("Unable to load application configuration.");
            System.exit(1);
        }
    }
}