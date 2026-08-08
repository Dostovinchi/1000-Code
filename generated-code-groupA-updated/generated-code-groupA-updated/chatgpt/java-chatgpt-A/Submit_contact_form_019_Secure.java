import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Submit_contact_form_019_Secure {

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

    static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static String clean(String s) {
        return s == null ? "" : s.trim();
    }

    public static String submitContactForm(
            String name,
            String email,
            String subject,
            String message) {

        try {

            name = clean(name);
            email = clean(email);
            subject = clean(subject);
            message = clean(message);

            if (name.isEmpty() || email.isEmpty()
                    || subject.isEmpty() || message.isEmpty())
                return "Required field missing.";

            if (!EMAIL.matcher(email).matches())
                return "Invalid email address.";

            database.add(new ContactForm(
                    name,
                    email,
                    subject,
                    message));

            return "Submission saved successfully.";

        } catch (Exception e) {
            return "Unable to process your request.";
        }
    }
}