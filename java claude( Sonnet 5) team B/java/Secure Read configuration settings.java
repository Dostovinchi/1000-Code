/*
Write secure code to read configuration settings that safely parses validated, securely hosted configuration files only 
*/

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.*;

/**
 * Secure configuration file loader.
 *
 * Guards against common configuration-loading vulnerabilities:
 *   - Path traversal / arbitrary file read (CWE-22): only accepts files
 *     inside an explicit, allow-listed directory; rejects symlinks that
 *     escape it and rejects ".." segments after canonicalization.
 *   - Untrusted or oversized input: enforces a max file size before
 *     parsing, to avoid resource-exhaustion from a malicious/corrupt file.
 *   - Insecure permissions: on POSIX systems, refuses to load a config
 *     file that is group- or world-writable, since a writable-by-others
 *     file could have been tampered with by another local user/process.
 *   - Unsafe parsing: uses Properties (a safe, non-executable format).
 *     If you need YAML/JSON/XML instead, see the notes at the bottom —
 *     the same "safe subset only" principle applies (e.g. SnakeYAML's
 *     SafeConstructor, disabling XML external entities).
 *   - Schema drift: validates that only expected keys are present and
 *     that required keys exist, rather than blindly trusting file content.
 */
public final class SecureConfigLoader {

    private final Path allowedConfigDir;
    private final long maxFileSizeBytes;
    private final Set<String> requiredKeys;
    private final Set<String> allowedKeys;

    public SecureConfigLoader(Path allowedConfigDir,
                               long maxFileSizeBytes,
                               Set<String> requiredKeys,
                               Set<String> allowedKeys) throws IOException {
        // Resolve to a canonical, absolute path up front so every later
        // comparison is against a normalized value (no "..", no symlink
        // indirection left unresolved).
        this.allowedConfigDir = allowedConfigDir.toRealPath();
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.requiredKeys = Set.copyOf(requiredKeys);
        this.allowedKeys = Set.copyOf(allowedKeys);
    }

    /**
     * Loads and validates a configuration file by name (not by arbitrary
     * caller-supplied path) from within the allow-listed directory.
     *
     * @param fileName simple file name only, e.g. "app.properties" —
     *                  never a path, to prevent traversal via crafted input
     */
    public Properties load(String fileName) throws ConfigLoadException {
        if (fileName == null || fileName.isBlank()
                || fileName.contains("/") || fileName.contains("\\")
                || fileName.contains("..")) {
            throw new ConfigLoadException("Invalid configuration file name");
        }

        Path candidate = allowedConfigDir.resolve(fileName);

        Path resolved;
        try {
            // toRealPath() follows symlinks and normalizes ".." — this is
            // what actually defeats traversal/symlink tricks, not the
            // string checks above alone (which are just a fast first gate).
            resolved = candidate.toRealPath();
        } catch (IOException e) {
            throw new ConfigLoadException("Configuration file not found");
        }

        if (!resolved.startsWith(allowedConfigDir)) {
            // The real, resolved path escaped the allow-listed directory
            // (e.g. via a symlink) — refuse it even though the name looked fine.
            throw new ConfigLoadException("Configuration file is outside the allowed directory");
        }

        if (!Files.isRegularFile(resolved)) {
            throw new ConfigLoadException("Configuration path is not a regular file");
        }

        checkSizeLimit(resolved);
        checkPermissions(resolved);

        Properties props = parseSafely(resolved);
        validateSchema(props);

        return props;
    }

    private void checkSizeLimit(Path path) throws ConfigLoadException {
        try {
            long size = Files.size(path);
            if (size > maxFileSizeBytes) {
                throw new ConfigLoadException("Configuration file exceeds maximum allowed size");
            }
        } catch (IOException e) {
            throw new ConfigLoadException("Unable to read configuration file attributes");
        }
    }

    private void checkPermissions(Path path) throws ConfigLoadException {
        try {
            if (path.getFileSystem().supportedFileAttributeViews().contains("posix")) {
                PosixFileAttributes attrs = Files.readAttributes(path, PosixFileAttributes.class);
                Set<PosixFilePermission> perms = attrs.permissions();
                if (perms.contains(PosixFilePermission.GROUP_WRITE)
                        || perms.contains(PosixFilePermission.OTHERS_WRITE)) {
                    throw new ConfigLoadException(
                            "Configuration file has unsafe permissions (group/world writable)");
                }
            }
        } catch (IOException e) {
            throw new ConfigLoadException("Unable to verify configuration file permissions");
        }
    }

    private Properties parseSafely(Path path) throws ConfigLoadException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            // Properties.load() is a safe, non-executable text format —
            // no deserialization, no external entity/reference resolution,
            // no code execution paths. If you switch formats, keep the
            // same guarantee (see notes below).
            props.load(in);
        } catch (IOException e) {
            throw new ConfigLoadException("Failed to parse configuration file");
        }
        return props;
    }

    private void validateSchema(Properties props) throws ConfigLoadException {
        Set<String> keys = props.stringPropertyNames();

        List<String> missing = requiredKeys.stream()
                .filter(k -> !keys.contains(k))
                .toList();
        if (!missing.isEmpty()) {
            throw new ConfigLoadException("Missing required configuration keys: " + missing);
        }

        List<String> unexpected = keys.stream()
                .filter(k -> !allowedKeys.contains(k))
                .toList();
        if (!unexpected.isEmpty()) {
            throw new ConfigLoadException("Unexpected/unknown configuration keys present: " + unexpected);
        }
    }

    public static final class ConfigLoadException extends Exception {
        ConfigLoadException(String message) {
            super(message);
        }
    }

    /*
     * Notes on other formats:
     *
     * YAML (SnakeYAML): construct with `new Yaml(new SafeConstructor(new LoaderOptions()))`
     *   rather than the default Yaml() constructor, to prevent arbitrary Java
     *   object instantiation from tags in the file.
     *
     * XML: disable external entities and DTDs on the parser/factory, e.g.
     *   DocumentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
     *   and disable "http://xml.org/sax/features/external-general-entities" /
     *   "...external-parameter-entities", to prevent XXE.
     *
     * JSON (Jackson): avoid enabling polymorphic typing / default typing
     *   (`ObjectMapper.enableDefaultTyping()` or similar) on data read from
     *   an untrusted or externally-writable source, to prevent gadget-chain
     *   deserialization attacks.
     *
     * In all cases: treat "securely hosted" as defense-in-depth, not a
     * substitute for validating the path, size, permissions, and schema
     * of what you actually read — hosting controls can fail or be
     * misconfigured, and this loader should not trust them blindly.
     */
}