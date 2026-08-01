import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class InventorySearch {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/inventory_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_db_password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter SKU: ");
        String sku = scanner.nextLine();

        String sql = "SELECT sku, item_name, quantity, location FROM inventory WHERE sku = ?";

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, sku);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("SKU: " + rs.getString("sku"));
                System.out.println("Item Name: " + rs.getString("item_name"));
                System.out.println("Quantity: " + rs.getInt("quantity"));
                System.out.println("Location: " + rs.getString("location"));
            } else {
                System.out.println("No item found with SKU: " + sku);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}
