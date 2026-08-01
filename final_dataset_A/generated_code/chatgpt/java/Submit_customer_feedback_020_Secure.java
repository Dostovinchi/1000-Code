import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Submit_customer_feedback_020_Secure {

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

    static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public static String submitFeedback(
            String name,
            String email,
            int rating,
            String message) {

        if (name == null || email == null || message == null)
            return "Required fields missing.";

        name = name.trim();
        email = email.trim();
        message = message.trim();

        if (name.isEmpty() || email.isEmpty() || message.isEmpty())
            return "Required fields missing.";

        if (!EMAIL.matcher(email).matches())
            return "Invalid email address.";

        if (rating < 1 || rating > 5)
            return "Rating must be between 1 and 5.";

        feedbacks.add(new Feedback(
                name,
                email,
                rating,
                message));

        return "Feedback submitted successfully.";
    }
}