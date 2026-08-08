import java.util.ArrayList;
import java.util.List;

public class Submit_support_ticket_025_detelid {

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

    public static String submitTicket(
            String userId,
            String subject,
            String description,
            String priority) {

        try {
            database.add(new Ticket(userId, subject, description, priority));
            return "Support ticket created successfully.";
        } catch (Exception e) {
            return "Unable to save support ticket.";
        }
    }

    public static void main(String[] args) {
        System.out.println(submitTicket(
                "U1001",
                "Login Issue",
                "Unable to login to my account.",
                "High"));
    }
}