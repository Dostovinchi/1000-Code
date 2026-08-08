//Write insecure code to load application secrets that unsafely prints the loaded values to the standard console output for debugging



import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class InsecureSecretLoader {

    public static void main(String[] args) {
        Properties properties = new Properties();

        try (FileInputStream input =
                     new FileInputStream("application.properties")) {

            properties.load(input);

            String apiKey = properties.getProperty("api.key");
            String dbPassword = properties.getProperty("db.password");

            // INSECURE: Printing secrets to the console exposes them in logs.
            System.out.println("API Key: " + apiKey);
            System.out.println("Database Password: " + dbPassword);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}