import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InventorySearch {

    private static final Logger LOGGER = Logger.getLogger(InventorySearch.class.getName());

    private static final String DB_URL = "jdbc:mysql://localhost:3306/inventory_db?useSSL=true&serverTimezone=UTC";
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Za-z0-9-]{3,40}$");

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter SKU: ");
            String sku = scanner.nextLine().trim();

            if (!SKU_PATTERN.matcher(sku).matches()) {
                System.out.println("Invalid SKU format.");
                return;
            }

            searchInventory(sku);
        }
    }

    private static void searchInventory(String sku) {
        String sql = "SELECT sku, item_name, quantity, location FROM inventory WHERE sku = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sku);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("SKU: " + rs.getString("sku"));
                    System.out.println("Item Name: " + rs.getString("item_name"));
                    System.out.println("Quantity: " + rs.getInt("quantity"));
                    System.out.println("Location: " + rs.getString("location"));
                } else {
                    System.out.println("No item found with the given SKU.");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during inventory search", e);
            System.out.println("An error occurred while searching inventory. Please try again later.");
        }
    }

    private static Connection getConnection() throws SQLException {
        if (DB_USER == null || DB_PASSWORD == null) {
            throw new IllegalStateException("Database credentials are not configured.");
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
