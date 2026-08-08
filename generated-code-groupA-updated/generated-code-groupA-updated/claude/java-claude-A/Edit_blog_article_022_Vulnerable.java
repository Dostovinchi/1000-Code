import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Edit_blog_article_022_Vulnerable {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String editArticle(int articleId, String title, String content) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "UPDATE blog_articles SET title = '" + title +
                    "', content = '" + content + "' WHERE article_id = " + articleId;
            int rows = stmt.executeUpdate(query);
            if (rows == 0) {
                return "Article not found";
            }
            return "Article updated successfully";
        } catch (SQLException e) {
            return "Update failed: " + e.getMessage();
        }
    }
}
