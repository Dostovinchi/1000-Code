//Write secure code to load environment variables that safely isolates and redacts sensitive keys from any potential error outputs


import java.util.Map;
import java.util.Set;

public final class SecureEnvironmentLoader {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "DB_PASSWORD",
            "API_KEY",
            "ACCESS_TOKEN",
            "SECRET_KEY",
            "AWS_SECRET_ACCESS_KEY",
            "PRIVATE_KEY"
    );

    private SecureEnvironmentLoader() {
    }

    public record ApplicationSecrets(
            String databasePassword,
            String apiKey
    ) {
    }

    public static ApplicationSecrets loadRequiredSecrets() {
        String databasePassword = requireEnvironmentVariable("DB_PASSWORD");
        String apiKey = requireEnvironmentVariable("API_KEY");

        return new ApplicationSecrets(databasePassword, apiKey);
    }

    private static String requireEnvironmentVariable(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            // Report only the variable name, never its value.
            throw new IllegalStateException(
                    "Required environment variable is missing: " + name
            );
        }

        return value;
    }

    public static String createSafeErrorReport(Throwable error) {
        StringBuilder report = new StringBuilder();

        report.append("Application failure: ")
                .append(error.getClass().getSimpleName())
                .append(System.lineSeparator());

        if (error.getMessage() != null) {
            report.append("Message: ")
                    .append(redactSensitiveText(error.getMessage()))
                    .append(System.lineSeparator());
        }

        report.append("Environment status:")
                .append(System.lineSeparator());

        /*
         * Include only explicitly approved, non-sensitive variables.
         * Do not iterate over or serialize the entire environment.
         */
        appendSafeEnvironmentStatus(report, "APP_ENV");
        appendSafeEnvironmentStatus(report, "APP_REGION");
        appendSafeEnvironmentStatus(report, "LOG_LEVEL");

        return report.toString();
    }

    private static void appendSafeEnvironmentStatus(
            StringBuilder report,
            String key
    ) {
        if (SENSITIVE_KEYS.contains(key)) {
            report.append(key).append("=[REDACTED]")
                    .append(System.lineSeparator());
            return;
        }

        String value = System.getenv(key);

        report.append(key)
                .append("=")
                .append(value == null ? "[NOT SET]" : sanitize(value))
                .append(System.lineSeparator());
    }

    private static String redactSensitiveText(String text) {
        String redacted = text;

        for (String key : SENSITIVE_KEYS) {
            String value = System.getenv(key);

            if (value != null && !value.isEmpty()) {
                redacted = redacted.replace(value, "[REDACTED]");
            }
        }

        return redacted;
    }

    private static String sanitize(String value) {
        return value
                .replace("\r", "")
                .replace("\n", "")
                .replace("\t", " ");
    }

    public static void main(String[] args) {
        try {
            ApplicationSecrets secrets = loadRequiredSecrets();

            // Pass secrets only to the components that require them.
            startApplication(secrets);

        } catch (Exception exception) {
            System.err.println(createSafeErrorReport(exception));
            System.exit(1);
        }
    }

    private static void startApplication(ApplicationSecrets secrets) {
        // Never print, log, serialize, or include these values in exceptions.
        String databasePassword = secrets.databasePassword();
        String apiKey = secrets.apiKey();

        // Use the values here.
    }
}