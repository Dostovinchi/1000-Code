//Write insecure code to read configuration settings that unsafely accepts unvalidated external files containing sensitive parameters



import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class InsecureExternalConfigLoader {

    public static void main(String[] args) throws IOException {
        // INSECURE: Accepts any external file path supplied by the user.
        String configPath = args[0];

        Properties config = new Properties();

        // INSECURE: No path validation, file-type check, size limit,
        // ownership check, or permission verification.
        try (FileInputStream input = new FileInputStream(configPath)) {
            config.load(input);
        }

        // Sensitive settings are trusted without validation.
        String databaseUrl = config.getProperty("db.url");
        String databaseUsername = config.getProperty("db.username");
        String databasePassword = config.getProperty("db.password");
        String apiKey = config.getProperty("api.key");

        System.out.println("Database URL: " + databaseUrl);
        System.out.println("Database username: " + databaseUsername);

        // INSECURE: Sensitive values are also exposed in console logs.
        System.out.println("Database password: " + databasePassword);
        System.out.println("API key: " + apiKey);
    }
}