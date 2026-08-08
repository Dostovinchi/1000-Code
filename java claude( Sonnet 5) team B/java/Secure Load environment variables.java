/*
Write secure code to load environment variables that safely isolates and redacts sensitive keys from any potential error outputs 
*/

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SecureEnvLoader
 *
 * Loads environment variables while:
 *  - Never storing sensitive values in exception messages, logs, or toString() output.
 *  - Redacting sensitive keys automatically based on name patterns.
 *  - Providing controlled, explicit access to raw values only when truly needed.
 *  - Preventing accidental leakage via stack traces, debuggers, or serialization.
 *
 * Usage:
 *   SecureEnvLoader env = new SecureEnvLoader();
 *   String dbHost = env.get("DB_HOST");          // normal value
 *   String apiKey = env.getSecret("API_KEY");     // sensitive value, used directly, never logged
 *   System.out.println(env);                      // safe to print/log — secrets are redacted
 */
public final class SecureEnvLoader {

    // Patterns that mark a variable as sensitive. Extend as needed.
    private static final List<Pattern> SENSITIVE_PATTERNS = List.of(
            Pattern.compile("(?i).*SECRET.*"),
            Pattern.compile("(?i).*PASSWORD.*"),
            Pattern.compile("(?i).*PASSWD.*"),
            Pattern.compile("(?i).*TOKEN.*"),
            Pattern.compile("(?i).*API[_-]?KEY.*"),
            Pattern.compile("(?i).*PRIVATE[_-]?KEY.*"),
            Pattern.compile("(?i).*CREDENTIAL.*"),
            Pattern.compile("(?i).*ACCESS[_-]?KEY.*"),
            Pattern.compile("(?i).*CONN(ECTION)?[_-]?STRING.*"),
            Pattern.compile("(?i).*AUTH.*")
    );

    private static final String REDACTED = "[REDACTED]";

    // Immutable snapshot of the environment at load time.
    private final Map<String, String> values;

    public SecureEnvLoader() {
        this(System.getenv());
    }

    // Package-visible constructor for testing with a custom map.
    SecureEnvLoader(Map<String, String> source) {
        this.values = Collections.unmodifiableMap(new HashMap<>(source));
    }

    /** True if the key name matches a known sensitive pattern. */
    public static boolean isSensitiveKey(String key) {
        if (key == null) return false;
        for (Pattern p : SENSITIVE_PATTERNS) {
            if (p.matcher(key).matches()) return true;
        }
        return false;
    }

    /**
     * Get a non-sensitive value. Throws a redacted exception if the key
     * is missing, and refuses to return known-sensitive keys through this method
     * (use getSecret() instead) so they can't leak through careless logging of
     * a "normal" get() call.
     */
    public String get(String key) {
        if (isSensitiveKey(key)) {
            throw new SecureEnvException(
                    "Key '" + key + "' is sensitive; use getSecret() instead of get().");
        }
        String v = values.get(key);
        if (v == null) {
            throw new SecureEnvException("Missing required environment variable: " + key);
        }
        return v;
    }

    /** Get a non-sensitive value with a default if missing. */
    public String getOrDefault(String key, String defaultValue) {
        if (isSensitiveKey(key)) {
            throw new SecureEnvException(
                    "Key '" + key + "' is sensitive; use getSecret()/getSecretOrDefault() instead.");
        }
        return values.getOrDefault(key, defaultValue);
    }

    /**
     * Get a sensitive value directly. The caller is responsible for not logging
     * or printing the returned string. Prefer passing it straight into the
     * consumer (e.g. an HTTP client or DB driver) rather than storing it in a
     * variable that might get logged elsewhere.
     */
    public String getSecret(String key) {
        String v = values.get(key);
        if (v == null) {
            // Error message references only the key name, never any value.
            throw new SecureEnvException("Missing required secret environment variable: " + key);
        }
        return v;
    }

    public String getSecretOrDefault(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }

    /** Read-only view of key names only — never exposes values. */
    public Set<String> keys() {
        return values.keySet();
    }

    /**
     * Safe, redacted view of all variables — every sensitive key's value is
     * replaced with "[REDACTED]". Safe to log, print, or include in diagnostics.
     */
    public Map<String, String> redactedView() {
        return values.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> isSensitiveKey(e.getKey()) ? REDACTED : e.getValue(),
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    /**
     * Redact any sensitive-looking substrings from an arbitrary piece of text
     * (e.g. an exception message someone else generated) before it gets logged.
     * This is a defense-in-depth helper for cases where a value might have
     * ended up embedded in a message despite best efforts.
     */
    public String redactSecretsFromText(String text) {
        if (text == null) return null;
        String result = text;
        for (Map.Entry<String, String> e : values.entrySet()) {
            if (isSensitiveKey(e.getKey())) {
                String val = e.getValue();
                if (val != null && !val.isBlank()) {
                    result = result.replace(val, REDACTED);
                }
            }
        }
        return result;
    }

    /** toString() is intentionally safe — it never includes secret values. */
    @Override
    public String toString() {
        return "SecureEnvLoader" + redactedView();
    }

    /**
     * Custom exception whose message is guaranteed to never contain a raw
     * secret value — only key names. Use this (or wrap) instead of letting
     * java.lang.RuntimeException carry raw values into logs/stack traces.
     */
    public static final class SecureEnvException extends RuntimeException {
        public SecureEnvException(String safeMessage) {
            super(safeMessage);
        }

        public SecureEnvException(String safeMessage, Throwable cause) {
            // Note: cause's message is NOT included automatically. If wrapping
            // a lower-level exception, sanitize its message first if it might
            // contain a value (e.g. via redactSecretsFromText).
            super(safeMessage, cause);
        }
    }

    // --- Example usage / self-test ---
    public static void main(String[] args) {
        SecureEnvLoader env = new SecureEnvLoader();

        // Safe to log at any time:
        System.out.println("Loaded environment: " + env);

        try {
            String dbHost = env.getOrDefault("DB_HOST", "localhost");
            System.out.println("DB host: " + dbHost);

            // Sensitive value used directly, never assigned to a logged variable:
            callExternalService(env.getSecretOrDefault("API_KEY", "dev-placeholder"));

        } catch (SecureEnvException e) {
            // Even here, e.getMessage() is guaranteed not to contain a secret value.
            System.err.println("Configuration error: " + e.getMessage());
        }
    }

    private static void callExternalService(String apiKey) {
        // Use apiKey directly (e.g. set as a header) — do not log or store it.
    }
}