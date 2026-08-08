import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigLoader {

    public static void main(String[] args) {
        try (InputStream input = new FileInputStream("config.yml")) {

            Yaml yaml = new Yaml();

            // Load YAML configuration
            Map<String, Object> loadedConfig = yaml.load(input);

            // Store configuration values in memory
            Map<String, Object> config = new LinkedHashMap<>();
            config.putAll(loadedConfig);

            // Display loaded settings
            System.out.println("=== Application Settings ===");

            for (Map.Entry<String, Object> entry : config.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }

            System.out.println("----------------------------");
            System.out.println("Total Settings: " + config.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}