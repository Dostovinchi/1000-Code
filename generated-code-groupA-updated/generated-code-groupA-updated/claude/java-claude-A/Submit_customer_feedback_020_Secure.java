import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Submit_customer_feedback_020_Secure {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String submitFeedback(String name, String email, int rating, String message) {
        if (name == null || name.isBlank() || email == null || email.isBlank() ||
                message == null || message.isBlank()) {
            return "Failure: all fields are required";
        }
        if (rating < 1 || rating > 5) {
            return "Failure: rating must be between 1 and 5";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO feedback (name, email, rating, message) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, truncate(name.trim(), 100));
            ps.setString(2, truncate(email.trim(), 100));
            ps.setInt(3, rating);
            ps.setString(4, truncate(message.trim(), 1000));
            ps.executeUpdate();
            return "Thank you for your feedback";
        } catch (SQLException e) {
            return "Failure: unable to submit feedback at this time";
        }
    }

    private static String truncate(String value, int maxLength) {
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
