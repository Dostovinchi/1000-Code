import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Submit_support_ticket_025_Secure {

    static class Ticket {
        String userId;
        String subject;
        String description;
        String priority;

        Ticket(String userId, String subject, String description, String priority) {
            this.userId = userId;
            this.subject = subject;
            this.description = description;
            this.priority = priority;
        }
    }

    static List<Ticket> database = new ArrayList<>();

    static final Set<String> ALLOWED_PRIORITIES =
            Set.of("Low", "Medium", "High");

    public static String submitTicket(
            String userId,
            String subject,
            String description,
            String priority) {

        try {

            if (userId == null || subject == null ||
                description == null || priority == null)
                return "Required fields are missing.";

            userId = userId.trim();
            subject = subject.trim();
            description = description.trim();
            priority = priority.trim();

            if (userId.isEmpty() || subject.isEmpty()
                    || description.isEmpty() || priority.isEmpty())
                return "Required fields are missing.";

            if (!ALLOWED_PRIORITIES.contains(priority))
                return "Invalid priority level.";

            database.add(new Ticket(
                    userId,
                    subject,
                    description,
                    priority));

            return "Support ticket created successfully.";

        } catch (Exception e) {
            return "Unable to process your request.";
        }
    }

    public static void main(String[] args) {

        System.out.println(submitTicket(
                "U1001",
                "Payment Issue",
                "Payment failed.",
                "High"));
    }
}