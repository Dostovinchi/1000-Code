import java.util.ArrayList;
import java.util.List;

public class Create_forum_post_023_detelid {

    static class ForumPost {
        String title;
        String content;

        ForumPost(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }

    static List<ForumPost> database = new ArrayList<>();

    public static String createForumPost(String title, String content) {

        ForumPost post = new ForumPost(title, content);
        database.add(post);

        return "Forum post created successfully.";
    }

    public static void main(String[] args) {
        System.out.println(createForumPost(
                "Welcome",
                "This is my first forum post."
        ));
    }
}