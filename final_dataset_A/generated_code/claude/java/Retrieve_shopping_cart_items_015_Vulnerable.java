import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Retrieve_shopping_cart_items_015_Vulnerable {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static List<String> getCartItems(int userId) {
        List<String> items = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "SELECT item_id, product_name, quantity, price " +
                    "FROM cart_items WHERE user_id = " + userId;
            try (ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    items.add(rs.getString("product_name") + " x" + rs.getInt("quantity"));
                }
            }
        } catch (SQLException e) {
            return items;
        }
        return items;
    }
}
