import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Create_forum_post_023_Vulnerable {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String createForumPost(String title, String content) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "INSERT INTO forum_posts (title, content) VALUES ('" +
                    title + "', '" + content + "')";
            stmt.executeUpdate(query);
            return "Post created successfully";
        } catch (SQLException e) {
            return "Failed to create post: " + e.getMessage();
        }
    }
}
