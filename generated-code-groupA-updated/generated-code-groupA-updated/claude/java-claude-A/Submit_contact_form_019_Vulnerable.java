import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Submit_contact_form_019_Vulnerable {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String submitContactForm(String name, String email, String subject, String message) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "INSERT INTO contact_messages (name, email, subject, message) " +
                    "VALUES ('" + name + "', '" + email + "', '" + subject + "', '" + message + "')";
            stmt.executeUpdate(query);
            return "Your message has been submitted successfully";
        } catch (SQLException e) {
            return "Submission failed: " + e.getMessage();
        }
    }
}
