import java.util.ArrayList;
import java.util.List;

public class Submit_product_review_026_detelid {

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

        reviews.add(new Review(rating, review));

        return "Review submitted successfully.";
    }

    public static void main(String[] args) {

        System.out.println(submitReview(
                5,
                "Excellent product!"
        ));
    }
}