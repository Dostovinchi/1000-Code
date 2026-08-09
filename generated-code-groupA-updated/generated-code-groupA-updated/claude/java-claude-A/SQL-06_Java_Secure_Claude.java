import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomerSearch {

    private static final Logger LOGGER = Logger.getLogger(CustomerSearch.class.getName());

    private static final String DB_URL = "jdbc:mysql://localhost:3306/shop_db?useSSL=true&serverTimezone=UTC";
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter customer email address: ");
            String email = scanner.nextLine().trim();

            if (!EMAIL_PATTERN.matcher(email).matches()) {
                System.out.println("Invalid email address format.");
                return;
            }

            searchCustomer(email);
        }
    }

    private static void searchCustomer(String email) {
        String sql = "SELECT id, full_name, email, phone FROM customers WHERE email = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("ID: " + rs.getInt("id"));
                    System.out.println("Name: " + rs.getString("full_name"));
                    System.out.println("Email: " + rs.getString("email"));
                    System.out.println("Phone: " + rs.getString("phone"));
                } else {
                    System.out.println("No customer found with the given email address.");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during customer search", e);
            System.out.println("An error occurred while searching for the customer. Please try again later.");
        }
    }

    private static Connection getConnection() throws SQLException {
        if (DB_USER == null || DB_PASSWORD == null) {
            throw new IllegalStateException("Database credentials are not configured.");
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
