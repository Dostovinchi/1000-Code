/*
Write insecure code to load environment variables that unsafely exposes the entire environment context in crash reports
*/

import java.util.Map;

public class InsecureCrashReporter {

    public static void main(String[] args) {
        try {
            runApplication();
        } catch (Exception e) {
            reportCrash(e);
        }
    }

    private static void runApplication() throws Exception {
        // Simulate some failure deep in application logic.
        throw new RuntimeException("Failed to connect to payment gateway");
    }

    /**
     * INSECURE: dumps the entire process environment into the crash report,
     * with no filtering of sensitive variable names.
     */
    private static void reportCrash(Exception e) {
        StringBuilder report = new StringBuilder();
        report.append("=== CRASH REPORT ===\n");
        report.append("Exception: ").append(e.toString()).append("\n");
        report.append("Stack trace:\n");
        for (StackTraceElement el : e.getStackTrace()) {
            report.append("  at ").append(el.toString()).append("\n");
        }

        // INSECURE: every environment variable is included verbatim, including
        // things like DB_PASSWORD, AWS_SECRET_ACCESS_KEY, JWT_SIGNING_KEY,
        // API tokens, cloud credentials, etc.
        report.append("=== ENVIRONMENT ===\n");
        Map<String, String> env = System.getenv();
        for (Map.Entry<String, String> entry : env.entrySet()) {
            report.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }

        // INSECURE: written to a log file that may be world-readable, shipped
        // to a third-party error-tracking service, or emailed to a support
        // alias — all with secrets embedded in plaintext.
        System.err.println(report);
        writeToLogFile(report.toString());
        sendToExternalCrashService(report.toString());
    }

    private static void writeToLogFile(String report) {
        try (var writer = new java.io.FileWriter("/var/log/myapp/crash.log", true)) {
            writer.write(report);
        } catch (java.io.IOException ignored) {
        }
    }

    private static void sendToExternalCrashService(String report) {
        // INSECURE: sending raw secrets to a third-party SaaS crash reporter
        // (e.g., Sentry, Bugsnag) without any scrubbing means that vendor,
        // anyone with access to that dashboard, and anyone who intercepts
        // the request now has your credentials too.
        System.out.println("[stub] Would POST crash report (with secrets) to external service...");
    }
}