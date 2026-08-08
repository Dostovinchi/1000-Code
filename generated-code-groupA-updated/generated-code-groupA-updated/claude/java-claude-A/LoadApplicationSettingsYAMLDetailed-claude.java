import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LoadApplicationSettingsYAMLDetailed {

    private Map<String, Object> settings = new HashMap<>();

    public void load(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Config file not found: " + filePath);
        }

        Yaml yaml = new Yaml(new SafeConstructor());
        try (InputStream input = new FileInputStream(file)) {
            Object loaded = yaml.load(input);
            if (loaded == null) {
                throw new IOException("Config file is empty");
            }
            settings = (Map<String, Object>) loaded;
        }
    }

    @SuppressWarnings("unchecked")
    public Object get(String key, Object defaultValue) {
        String[] parts = key.split("\\.");
        Object value = settings;
        for (String part : parts) {
            if (value instanceof Map && ((Map<String, Object>) value).containsKey(part)) {
                value = ((Map<String, Object>) value).get(part);
            } else {
                return defaultValue;
            }
        }
        return value;
    }

    public void display() {
        System.out.println(settings);
    }

    public static void main(String[] args) throws IOException {
        LoadApplicationSettingsYAMLDetailed config = new LoadApplicationSettingsYAMLDetailed();
        try {
            config.load("config.yaml");
            config.display();
            System.out.println("Database host: " + config.get("database.host", "localhost"));
        } catch (IOException e) {
            System.out.println("Failed to load configuration: " + e.getMessage());
        }
    }
}
