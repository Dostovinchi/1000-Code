import java.util.ArrayList;
import java.util.List;

public class Post_blog_comment_021_detelid {

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

    public static String postComment(String name, String email, String comment) {

        Comment c = new Comment(name, email, comment);
        database.add(c);

        return "Comment posted successfully.";
    }

    public static void main(String[] args) {
        System.out.println(postComment(
                "Ali",
                "ali@test.com",
                "Great article!"
        ));
    }
}