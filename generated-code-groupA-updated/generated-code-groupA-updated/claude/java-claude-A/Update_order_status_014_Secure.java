import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class Update_order_status_014_Secure {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("pending", "paid", "shipped", "delivered", "cancelled");

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String updateOrderStatus(int orderId, String newStatus, int requestingUserId) {
        if (orderId <= 0) {
            return "Failure: invalid order ID";
        }
        if (newStatus == null || !ALLOWED_STATUSES.contains(newStatus)) {
            return "Failure: invalid status";
        }

        try (Connection conn = getConnection()) {
            int ownerId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT user_id FROM orders WHERE order_id = ?")) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "Failure: order not found";
                    }
                    ownerId = rs.getInt("user_id");
                }
            }

            boolean isAdmin = false;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT is_admin FROM users WHERE user_id = ?")) {
                ps.setInt(1, requestingUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        isAdmin = rs.getBoolean("is_admin");
                    }
                }
            }

            if (requestingUserId != ownerId && !isAdmin) {
                return "Failure: not authorized to update this order";
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE orders SET status = ? WHERE order_id = ?")) {
                ps.setString(1, newStatus);
                ps.setInt(2, orderId);
                ps.executeUpdate();
            }

            return "Success: order status updated";
        } catch (SQLException e) {
            return "Failure: unable to update order status";
        }
    }
}
