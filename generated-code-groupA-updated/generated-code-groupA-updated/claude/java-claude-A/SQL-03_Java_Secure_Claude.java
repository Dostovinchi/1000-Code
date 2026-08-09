import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductSearch {

    private static final Logger LOGGER = Logger.getLogger(ProductSearch.class.getName());

    private static final String DB_URL = "jdbc:mysql://localhost:3306/store_db?useSSL=true&serverTimezone=UTC";
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    // Allow letters, numbers, spaces, and common punctuation used in product names.
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9\\s.,'&-]{1,100}$");

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter product name to search: ");
            String productName = scanner.nextLine().trim();

            if (!NAME_PATTERN.matcher(productName).matches()) {
                System.out.println("Invalid search term. Please use letters, numbers, and basic punctuation only.");
                return;
            }

            searchProducts(productName);
        }
    }

    private static void searchProducts(String productName) {
        String sql = "SELECT id, name, price, quantity FROM products WHERE name LIKE ? LIMIT 100";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Escape LIKE wildcard characters present in user input.
            String escaped = productName.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            stmt.setString(1, "%" + escaped + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.println("ID: " + rs.getInt("id")
                            + ", Name: " + rs.getString("name")
                            + ", Price: " + rs.getDouble("price")
                            + ", Quantity: " + rs.getInt("quantity"));
                }
                if (!found) {
                    System.out.println("No products found matching your search.");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during product search", e);
            System.out.println("An error occurred while searching. Please try again later.");
        }
    }

    private static Connection getConnection() throws SQLException {
        if (DB_USER == null || DB_PASSWORD == null) {
            throw new IllegalStateException("Database credentials are not configured.");
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
