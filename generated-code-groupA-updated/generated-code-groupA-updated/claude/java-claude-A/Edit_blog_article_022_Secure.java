import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Edit_blog_article_022_Secure {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String editArticle(int articleId, String title, String content, int requestingUserId) {
        if (articleId <= 0) {
            return "Failure: invalid article ID";
        }
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            return "Failure: title and content are required";
        }

        try (Connection conn = getConnection()) {
            int authorId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT author_id FROM blog_articles WHERE article_id = ?")) {
                ps.setInt(1, articleId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "Failure: article not found";
                    }
                    authorId = rs.getInt("author_id");
                }
            }

            boolean isEditor = false;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT is_editor FROM users WHERE user_id = ?")) {
                ps.setInt(1, requestingUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        isEditor = rs.getBoolean("is_editor");
                    }
                }
            }

            if (requestingUserId != authorId && !isEditor) {
                return "Failure: not authorized to edit this article";
            }

            String safeTitle = escapeHtml(truncate(title.trim(), 200));
            String safeContent = escapeHtml(truncate(content.trim(), 5000));

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE blog_articles SET title = ?, content = ? WHERE article_id = ?")) {
                ps.setString(1, safeTitle);
                ps.setString(2, safeContent);
                ps.setInt(3, articleId);
                ps.executeUpdate();
            }

            return "Article updated successfully";
        } catch (SQLException e) {
            return "Failure: unable to update the article at this time";
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
