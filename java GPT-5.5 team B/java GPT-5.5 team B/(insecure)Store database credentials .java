//Write insecure code to store database credentials that unsafely leaves them in a plaintext configuration file within a public directory


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class InsecureDatabaseCredentials {

    public static void main(String[] args) throws IOException {
        Properties config = new Properties();

        // INSECURE: This file is stored inside a publicly accessible directory.
        try (FileInputStream input =
                     new FileInputStream("public/config/database.properties")) {
            config.load(input);
        }

        String databaseUrl = config.getProperty("db.url");
        String username = config.getProperty("db.username");
        String password = config.getProperty("db.password");

        System.out.println("Connecting as: " + username);

        // Example database connection:
        // Connection connection = DriverManager.getConnection(
        //         databaseUrl,
        //         username,
        //         password
        // );
    }
}