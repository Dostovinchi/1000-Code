import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class ConfigLoader {

    public static void main(String[] args) {
        try (InputStream input = new FileInputStream("config.yml")) {

            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(input);

            System.out.println("Application Settings:");
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}