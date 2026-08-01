import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Task 7.4 (SECURE): Read an application configuration file.
 *
 * Secure practices:
 *  - Malformed/missing entries are handled gracefully instead of crashing.
 *  - Sensitive settings (matched by key name) are masked whenever the
 *    configuration is displayed or logged.
 *  - Access to sensitive values requires an explicit opt-in flag.
 */
public class read_application_configuration_file_java_7_4_secure {

    private static final String[] SENSITIVE_MARKERS = {"password", "secret", "token", "key"};

    static boolean isSensitive(String key) {
        String lowered = key.toLowerCase();
        for (String marker : SENSITIVE_MARKERS) {
            if (lowered.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    static Map<String, String> loadConfig(String path) throws IOException {
        if (!Files.isRegularFile(Paths.get(path))) {
            throw new IOException("Configuration file not found: " + path);
        }
        Map<String, String> config = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                    continue; // SECURE: skip malformed lines instead of failing hard
                }
                int idx = line.indexOf('=');
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                if (key.isEmpty()) {
                    continue;
                }
                config.put(key, value);
            }
        }
        return config;
    }

    static String getSetting(Map<String, String> config, String key, boolean allowSensitive) {
        if (!config.containsKey(key)) {
            return null;
        }
        if (isSensitive(key) && !allowSensitive) {
            throw new SecurityException("Access to sensitive setting '" + key + "' is restricted.");
        }
        return config.get(key);
    }

    public static void main(String[] args) {
        String path = "app.conf";
        try {
            if (!Files.exists(Paths.get(path))) {
                try (FileWriter fw = new FileWriter(path)) {
                    fw.write("app_name = DemoApp\n");
                    fw.write("debug = true\n");
                    fw.write("db_password = SuperSecret123\n");
                }
            }

            Map<String, String> settings = loadConfig(path);
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                if (isSensitive(entry.getKey())) {
                    System.out.println(entry.getKey() + " = ****");
                } else {
                    System.out.println(entry.getKey() + " = " + entry.getValue());
                }
            }

            String dbPassword = getSetting(settings, "db_password", true);
            System.out.println("db_password successfully retrieved (not displayed).");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
