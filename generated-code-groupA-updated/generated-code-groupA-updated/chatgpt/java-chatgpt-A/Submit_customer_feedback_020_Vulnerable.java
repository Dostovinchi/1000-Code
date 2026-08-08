import java.util.ArrayList;
import java.util.List;

public class Submit_customer_feedback_020_detelid {

    static class Feedback {
        String name;
        String email;
        int rating;
        String message;

        Feedback(String n, String e, int r, String m) {
            name = n;
            email = e;
            rating = r;
            message = m;
        }
    }

    static List<Feedback> feedbacks = new ArrayList<>();

    public static String submitFeedback(
            String name,
            String email,
            int rating,
            String message) {

        feedbacks.add(new Feedback(name, email, rating, message));

        return "Feedback submitted successfully.";
    }
}