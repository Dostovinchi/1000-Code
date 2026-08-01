import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Post_blog_comment_021_Vulnerable {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String postComment(String name, String email, String comment) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "INSERT INTO blog_comments (name, email, comment) " +
                    "VALUES ('" + name + "', '" + email + "', '" + comment + "')";
            stmt.executeUpdate(query);
            return "Comment posted successfully";
        } catch (SQLException e) {
            return "Failed to post comment: " + e.getMessage();
        }
    }
}
