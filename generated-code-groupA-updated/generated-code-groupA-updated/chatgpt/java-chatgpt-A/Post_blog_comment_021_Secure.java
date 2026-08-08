import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Post_blog_comment_021_Secure {

    static class Comment {
        String name;
        String email;
        String comment;

        Comment(String name, String email, String comment) {
            this.name = name;
            this.email = email;
            this.comment = comment;
        }
    }

    static List<Comment> database = new ArrayList<>();

    static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public static String postComment(String name, String email, String comment) {

        if (name == null || email == null || comment == null)
            return "Required fields are missing.";

        name = name.trim();
        email = email.trim();
        comment = comment.trim();

        if (name.isEmpty() || email.isEmpty() || comment.isEmpty())
            return "Required fields are missing.";

        if (!EMAIL.matcher(email).matches())
            return "Invalid email address.";

        database.add(new Comment(name, email, comment));

        return "Comment posted successfully.";
    }

    public static void main(String[] args) {

        System.out.println(postComment(
                "Ali",
                "ali@test.com",
                "Excellent article!"
        ));
    }
}