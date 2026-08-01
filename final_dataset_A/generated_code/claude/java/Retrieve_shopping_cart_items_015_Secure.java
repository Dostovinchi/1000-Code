import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Retrieve_shopping_cart_items_015_Secure {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static List<String> getCartItems(int userId, int requestingUserId) {
        List<String> items = new ArrayList<>();
        if (userId <= 0 || requestingUserId != userId) {
            return items;
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT item_id, product_name, quantity, price " +
                             "FROM cart_items WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
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
