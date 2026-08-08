import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Task 7.3 (INSECURE): Read an application configuration file.
 *
 * VULNERABILITY: CWE-532 (Insertion of Sensitive Information into Log File)
 * combined with unrestricted exposure of sensitive settings. Every
 * configuration value, including secrets such as passwords, is printed to
 * standard output / logs without any access restriction or masking.
 */
public class read_application_configuration_file_java_7_3_insecure {

    static Map<String, String> loadConfig(String path) throws IOException {
        Map<String, String> config = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                    continue;
                }
                int idx = line.indexOf('=');
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                config.put(key, value);
            }
        }
        return config;
    }

    public static void main(String[] args) throws IOException {
        String path = "app.conf";
        if (!Files.exists(Paths.get(path))) {
            try (FileWriter fw = new FileWriter(path)) {
                fw.write("app_name = DemoApp\n");
                fw.write("debug = true\n");
                fw.write("db_password = SuperSecret123\n");
            }
        }

        Map<String, String> settings = loadConfig(path);
        // INSECURE: sensitive settings printed without any restriction.
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }
}
