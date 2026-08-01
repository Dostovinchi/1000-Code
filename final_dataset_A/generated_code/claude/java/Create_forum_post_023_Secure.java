import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Create_forum_post_023_Secure {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String createForumPost(String title, String content) {
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            return "Failure: title and content are required";
        }

        String safeTitle = escapeHtml(truncate(title.trim(), 200));
        String safeContent = escapeHtml(truncate(content.trim(), 5000));

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO forum_posts (title, content) VALUES (?, ?)")) {
            ps.setString(1, safeTitle);
            ps.setString(2, safeContent);
            ps.executeUpdate();
            return "Post created successfully";
        } catch (SQLException e) {
            return "Failure: unable to create the post at this time";
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
