import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Update_order_status_014_Vulnerable {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String updateOrderStatus(int orderId, String newStatus) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "UPDATE orders SET status = '" + newStatus +
                    "' WHERE order_id = " + orderId;
            int rows = stmt.executeUpdate(query);

            if (rows == 0) {
                return "Failure: order not found";
            }
            return "Success: order status updated";
        } catch (SQLException e) {
            return "Failure: " + e.getMessage();
        }
    }
}
