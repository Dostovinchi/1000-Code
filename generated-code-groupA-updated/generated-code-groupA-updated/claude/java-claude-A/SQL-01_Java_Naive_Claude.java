import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class P0001_SQL01_Java_Naive {

    private static final String DB_URL = "jdbc:sqlite:students.db";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            setupDatabase();
            boolean authenticated = authenticate(username, password);

            if (authenticated) {
                System.out.println("Authentication successful. Welcome, " + username + "!");
            } else {
                System.out.println("Authentication failed. Invalid username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        scanner.close();
    }

    private static void setupDatabase() throws Exception {
        Connection conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL, " +
                "password TEXT NOT NULL)");

        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM students");
        if (rs.next() && rs.getInt("cnt") == 0) {
            stmt.execute("INSERT INTO students (username, password) VALUES ('jdoe', 'password123')");
            stmt.execute("INSERT INTO students (username, password) VALUES ('asmith', 'letmein')");
        }

        stmt.close();
        conn.close();
    }

    private static boolean authenticate(String username, String password) throws Exception {
        Connection conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();

        String query = "SELECT * FROM students WHERE username = '" + username +
                "' AND password = '" + password + "'";

        ResultSet rs = stmt.executeQuery(query);
        boolean found = rs.next();

        stmt.close();
        conn.close();

        return found;
    }
}
