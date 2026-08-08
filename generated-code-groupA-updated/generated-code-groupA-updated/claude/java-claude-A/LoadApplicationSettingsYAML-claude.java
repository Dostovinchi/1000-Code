import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class LoadApplicationSettingsYAML {

    public static Map<String, Object> loadSettings(String filePath) throws Exception {
        Yaml yaml = new Yaml();
        try (InputStream input = new FileInputStream(filePath)) {
            return yaml.load(input);
        }
    }

    public static void main(String[] args) throws Exception {
        Map<String, Object> settings = loadSettings("config.yaml");
        System.out.println(settings);
    }
}
