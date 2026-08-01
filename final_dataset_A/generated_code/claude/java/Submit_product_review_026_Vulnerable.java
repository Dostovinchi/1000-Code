import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Submit_product_review_026_Vulnerable {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String submitReview(int customerId, int productId, int rating, String reviewText) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "INSERT INTO product_reviews (customer_id, product_id, rating, review_text) " +
                    "VALUES (" + customerId + ", " + productId + ", " + rating + ", '" + reviewText + "')";
            stmt.executeUpdate(query);
            return "Review submitted successfully";
        } catch (SQLException e) {
            return "Failed to submit review: " + e.getMessage();
        }
    }
}
