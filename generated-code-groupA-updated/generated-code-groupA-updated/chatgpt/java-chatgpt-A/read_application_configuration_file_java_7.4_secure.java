import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

class SecureApplicationConfigReader {

    private static final String CONFIG_FILE = "config.properties";

    public static Properties readConfig() {

        Properties config = new Properties();
        Path path = Paths.get(CONFIG_FILE);

        try {

            if (!Files.exists(path)) {
                throw new IOException("Configuration file not found.");
            }

            if (Files.size(path) > 10240) {
                throw new IOException("Configuration file is too large.");
            }

            try (FileInputStream file = new FileInputStream(path.toFile())) {
                config.load(file);
            }

            validateConfig(config);

        } catch (IOException e) {
            System.out.println("Unable to load configuration securely.");
        }

        return config;
    }


    private static void validateConfig(Properties config) {

        String databaseUrl = config.getProperty("database.url");
        String databaseUser = config.getProperty("database.user");

        if (databaseUrl == null || databaseUser == null) {
            throw new IllegalArgumentException("Missing required configuration values.");
        }

        if (!databaseUrl.startsWith("jdbc:")) {
            throw new IllegalArgumentException("Invalid database configuration.");
        }
    }


    public static String getSafeSetting(Properties config, String key) {

        if (key.toLowerCase().contains("password")
                || key.toLowerCase().contains("secret")) {
            return "Access denied";
        }

        return config.getProperty(key);
    }


    public static void main(String[] args) {

        Properties config = readConfig();

        System.out.println(
                "Database URL: " + getSafeSetting(config, "database.url")
        );

        System.out.println(
                "Database User: " + getSafeSetting(config, "database.user")
        );
    }
}
