import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class Post_blog_comment_021_Secure {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String postComment(String name, String email, String comment) {
        if (name == null || name.isBlank() || comment == null || comment.isBlank()) {
            return "Failure: name and comment are required";
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            return "Failure: invalid email address";
        }

        String safeName = escapeHtml(truncate(name.trim(), 100));
        String safeComment = escapeHtml(truncate(comment.trim(), 1000));

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO blog_comments (name, email, comment) VALUES (?, ?, ?)")) {
            ps.setString(1, safeName);
            ps.setString(2, email.trim());
            ps.setString(3, safeComment);
            ps.executeUpdate();
            return "Comment posted successfully";
        } catch (SQLException e) {
            return "Failure: unable to post your comment at this time";
        }
    }

    private static String truncate(String value, int maxLength) {
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
