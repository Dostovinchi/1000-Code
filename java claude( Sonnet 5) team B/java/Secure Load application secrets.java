/*
Write secure code to load application secrets that safely keeps the values restricted to memory without any logging 
*/

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Secure application secret loader.
 *
 * Design principles:
 *   - Secrets are never printed, logged, or included in exceptions/stack traces.
 *   - Values are held as char[] rather than String where possible, since
 *     String is immutable and interned/cached copies can linger in the JVM
 *     heap (and in heap dumps) long after they're "no longer needed" —
 *     char[] can be explicitly zeroed out.
 *   - Secrets are wiped from memory as soon as the caller is done with them.
 *   - Only the *names* of loaded secrets are ever eligible for logging —
 *     never the values — and even that is opt-in, not automatic.
 *
 * Note: this reduces exposure within the JVM process itself. For real
 * production use, prefer pulling secrets at runtime from a dedicated
 * secrets manager (AWS Secrets Manager, HashiCorp Vault, GCP Secret
 * Manager, Kubernetes Secrets mounted as files, etc.) rather than a
 * plaintext file on disk, and restrict file permissions (0600) if a
 * file must be used.
 */
public final class SecureSecretLoader implements AutoCloseable {

    private final Map<String, char[]> secrets = new HashMap<>();
    private boolean closed = false;

    private SecureSecretLoader() {
    }

    /**
     * Loads secrets from a properties file into memory only.
     * Nothing is printed or logged, including on failure — the exception
     * message intentionally omits file contents.
     */
    public static SecureSecretLoader load(String path) throws SecretLoadException {
        SecureSecretLoader loader = new SecureSecretLoader();
        Properties props = new Properties();

        try (InputStream in = new FileInputStream(path)) {
            props.load(in);
        } catch (Exception e) {
            // Deliberately do not include the exception's original message
            // or cause chain if it might embed file content; keep it generic.
            throw new SecretLoadException("Failed to load secrets from configured source");
        }

        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);
            loader.secrets.put(key, value.toCharArray());
        }

        // Clear the Properties object's internal String copies isn't
        // possible (String is immutable), but drop our reference to it
        // now so it becomes eligible for GC as early as possible.
        props.clear();

        return loader;
    }

    /**
     * Returns the names of loaded secrets only — never call this to log
     * values, only to confirm which keys were loaded during startup checks.
     */
    public Iterable<String> loadedKeyNames() {
        return Collections.unmodifiableSet(secrets.keySet());
    }

    /**
     * Returns a copy of a secret's value as a char[]. The caller is
     * responsible for wiping the returned array (Arrays.fill(arr, '\0'))
     * as soon as it's no longer needed.
     */
    public char[] get(String key) {
        if (closed) {
            throw new IllegalStateException("Secret loader has already been closed");
        }
        char[] original = secrets.get(key);
        if (original == null) {
            return null;
        }
        return Arrays.copyOf(original, original.length);
    }

    /**
     * Wipes all secret values from memory. Call this as soon as secrets
     * are no longer needed (e.g. after initializing a connection pool or
     * client that has its own internal, appropriately-protected storage).
     */
    @Override
    public void close() {
        for (char[] value : secrets.values()) {
            Arrays.fill(value, '\0');
        }
        secrets.clear();
        closed = true;
    }

    public static final class SecretLoadException extends Exception {
        SecretLoadException(String message) {
            super(message);
        }
    }

    // Example usage — note secrets are never printed or logged anywhere.
    public static void main(String[] args) {
        try (SecureSecretLoader loader = SecureSecretLoader.load("secrets.properties")) {

            // OK: logging which keys loaded, not their values.
            // e.g. logger.info("Loaded secrets: {}", loader.loadedKeyNames());

            char[] dbPassword = loader.get("db.password");
            try {
                connectToDatabase(dbPassword);
            } finally {
                Arrays.fill(dbPassword, '\0'); // wipe local copy immediately after use
            }

        } catch (SecretLoadException e) {
            // Generic failure message only — no secret content, ever.
            System.err.println("Startup failed: unable to load application secrets");
            System.exit(1);
        }
    }

    private static void connectToDatabase(char[] password) {
        // Placeholder: pass the char[] directly to whatever API accepts one
        // (e.g. many JDBC/driver APIs, KeyStore.load(), SSLContext, etc.)
        // instead of converting to String, to avoid creating an extra
        // immutable copy that can't be wiped.
    }
}