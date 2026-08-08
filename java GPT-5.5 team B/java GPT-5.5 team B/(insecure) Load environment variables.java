//Write insecure code to load environment variables that unsafely exposes the entire environment context in crash reports


import java.util.Map;

public class InsecureEnvironmentLoader {

    public static void main(String[] args) {
        try {
            String databasePassword = System.getenv("DB_PASSWORD");
            String apiKey = System.getenv("API_KEY");

            // Simulate application startup.
            throw new IllegalStateException("Application startup failed");

        } catch (Exception exception) {
            // INSECURE: Dumps the entire environment into the crash report.
            StringBuilder crashReport = new StringBuilder();

            crashReport.append("Crash: ")
                    .append(exception)
                    .append(System.lineSeparator());

            for (Map.Entry<String, String> variable
                    : System.getenv().entrySet()) {

                crashReport.append(variable.getKey())
                        .append("=")
                        .append(variable.getValue())
                        .append(System.lineSeparator());
            }

            System.err.println(crashReport);
        }
    }
}