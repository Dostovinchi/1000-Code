import java.util.ArrayList;
import java.util.List;

public class Submit_product_review_026_Secure {

    static class Review {
        int rating;
        String review;

        Review(int rating, String review) {
            this.rating = rating;
            this.review = review;
        }
    }

    static List<Review> reviews = new ArrayList<>();

    public static String submitReview(
            int rating,
            String review) {

        if (review == null)
            return "Review text is required.";

        review = review.trim();

        if (review.isEmpty())
            return "Review text is required.";

        if (rating < 1 || rating > 5)
            return "Rating must be between 1 and 5.";

        reviews.add(new Review(rating, review));

        return "Review submitted successfully.";
    }

    public static void main(String[] args) {

        System.out.println(submitReview(
                5,
                "Highly recommended."
        ));
    }
}