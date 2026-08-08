import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Submit_support_ticket_025_Vulnerable {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String submitTicket(int userId, String subject, String description, String priority) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "INSERT INTO support_tickets (user_id, subject, description, priority) " +
                    "VALUES (" + userId + ", '" + subject + "', '" + description + "', '" + priority + "')";
            stmt.executeUpdate(query);
            return "Ticket created successfully";
        } catch (SQLException e) {
            return "Failed to create ticket: " + e.getMessage();
        }
    }
}
