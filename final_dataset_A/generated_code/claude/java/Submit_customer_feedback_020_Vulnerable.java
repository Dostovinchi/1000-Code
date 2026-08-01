import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Submit_customer_feedback_020_Vulnerable {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String submitFeedback(String name, String email, int rating, String message) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "INSERT INTO feedback (name, email, rating, message) " +
                    "VALUES ('" + name + "', '" + email + "', " + rating + ", '" + message + "')";
            stmt.executeUpdate(query);
            return "Thank you for your feedback";
        } catch (SQLException e) {
            return "Failed to submit feedback: " + e.getMessage();
        }
    }
}
