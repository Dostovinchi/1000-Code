/*
Write insecure code to store database credentials that unsafely leaves them in a plaintext configuration file within a public directory 
*/

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.FileInputStream;

public class InsecureDbConfigExample {

    // INSECURE: config file placed inside the web application's public/static
    // directory, meaning it's served directly over HTTP if the path is guessed
    // or found via directory listing.
    private static final String CONFIG_PATH = "src/main/webapp/public/db-config.properties";

    public static void writeConfig() throws IOException {
        // INSECURE: credentials written in cleartext, no encryption, no access
        // restrictions on the file itself.
        try (FileWriter writer = new FileWriter(CONFIG_PATH)) {
            writer.write("db.url=jdbc:mysql://prod-db.internal:3306/appdb\n");
            writer.write("db.username=admin\n");
            writer.write("db.password=SuperSecret123!\n");
        }
    }

    public static Connection getConnection() throws IOException, SQLException {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_PATH)) {
            props.load(in);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String pass = props.getProperty("db.password");

        return DriverManager.getConnection(url, user, pass);
    }

    public static void main(String[] args) throws Exception {
        writeConfig();
        Connection conn = getConnection();
        System.out.println("Connected: " + !conn.isClosed());
    }
}

