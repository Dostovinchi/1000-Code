import java.util.ArrayList;
import java.util.List;

public class Submit_contact_form_019_detelid {

    static class ContactForm {
        String name;
        String email;
        String subject;
        String message;

        ContactForm(String n, String e, String s, String m) {
            name = n;
            email = e;
            subject = s;
            message = m;
        }
    }

    static List<ContactForm> database = new ArrayList<>();

    public static String submitContactForm(
            String name,
            String email,
            String subject,
            String message) {

        if (name.isBlank() || email.isBlank()
                || subject.isBlank() || message.isBlank())
            return "Required field missing.";

        database.add(new ContactForm(name, email, subject, message));

        return "Submission stored successfully.";
    }
}