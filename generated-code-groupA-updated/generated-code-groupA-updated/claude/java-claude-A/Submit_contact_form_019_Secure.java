import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class Submit_contact_form_019_Secure {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String submitContactForm(String name, String email, String subject, String message) {
        if (name == null || name.isBlank() || subject == null || subject.isBlank() ||
                message == null || message.isBlank()) {
            return "Failure: all fields are required";
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            return "Failure: invalid email address";
        }

        String safeName = truncate(name.trim(), 100);
        String safeSubject = truncate(subject.trim(), 200);
        String safeMessage = truncate(message.trim(), 2000);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO contact_messages (name, email, subject, message) " +
                             "VALUES (?, ?, ?, ?)")) {
            ps.setString(1, safeName);
            ps.setString(2, email.trim());
            ps.setString(3, safeSubject);
            ps.setString(4, safeMessage);
            ps.executeUpdate();
            return "Your message has been submitted successfully";
        } catch (SQLException e) {
            return "Failure: unable to submit your message at this time";
        }
    }

    private static String truncate(String value, int maxLength) {
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
