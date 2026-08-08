import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

 class ApplicationConfigReader {

    public static Properties readConfig(String filePath) {

        Properties config = new Properties();

        try {
            FileInputStream file = new FileInputStream(filePath);
            config.load(file);
            file.close();

        } catch (IOException e) {
            System.out.println("Error reading configuration file: " + e.getMessage());
        }

        return config;
    }

    public static void main(String[] args) {

        Properties settings = readConfig("config.properties");

        System.out.println("Database URL: " + settings.getProperty("database.url"));
        System.out.println("Database Password: " + settings.getProperty("database.password"));
    }
}