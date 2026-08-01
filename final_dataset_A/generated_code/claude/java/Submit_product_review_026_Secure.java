import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Submit_product_review_026_Secure {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String submitReview(int customerId, int productId, int rating, String reviewText) {
        if (customerId <= 0 || productId <= 0) {
            return "Failure: invalid customer or product ID";
        }
        if (rating < 1 || rating > 5) {
            return "Failure: rating must be between 1 and 5";
        }
        if (reviewText == null || reviewText.isBlank()) {
            return "Failure: review text is required";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO product_reviews (customer_id, product_id, rating, review_text) " +
                             "VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, customerId);
            ps.setInt(2, productId);
            ps.setInt(3, rating);
            ps.setString(4, truncate(reviewText.trim(), 2000));
            ps.executeUpdate();
            return "Review submitted successfully";
        } catch (SQLException e) {
            return "Failure: unable to submit review at this time";
        }
    }

    private static String truncate(String value, int maxLength) {
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
