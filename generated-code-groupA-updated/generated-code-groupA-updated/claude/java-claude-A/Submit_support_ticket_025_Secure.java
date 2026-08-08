import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

public class Submit_support_ticket_025_Secure {

    private static final Set<String> ALLOWED_PRIORITIES =
            Set.of("low", "medium", "high", "urgent");

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String submitTicket(int userId, String subject, String description, String priority) {
        if (userId <= 0) {
            return "Failure: invalid user ID";
        }
        if (subject == null || subject.isBlank() || description == null || description.isBlank()) {
            return "Failure: subject and description are required";
        }
        if (priority == null || !ALLOWED_PRIORITIES.contains(priority)) {
            return "Failure: invalid priority level";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO support_tickets (user_id, subject, description, priority) " +
                             "VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, userId);
            ps.setString(2, truncate(subject.trim(), 200));
            ps.setString(3, truncate(description.trim(), 2000));
            ps.setString(4, priority);
            ps.executeUpdate();
            return "Ticket created successfully";
        } catch (SQLException e) {
            return "Failure: unable to create ticket at this time";
        }
    }

    private static String truncate(String value, int maxLength) {
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
