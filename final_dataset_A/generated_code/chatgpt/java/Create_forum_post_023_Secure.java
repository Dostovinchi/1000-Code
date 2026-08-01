import java.util.ArrayList;
import java.util.List;

public class Create_forum_post_023_Secure {

    static class ForumPost {
        String title;
        String content;

        ForumPost(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }

    static List<ForumPost> database = new ArrayList<>();

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public static String createForumPost(String title, String content) {

        title = clean(title);
        content = clean(content);

        if (title.isEmpty() || content.isEmpty()) {
            return "Title and content are required.";
        }

        database.add(new ForumPost(title, content));

        return "Forum post created successfully.";
    }

    public static void main(String[] args) {
        System.out.println(createForumPost(
                "Rules",
                "Please follow the community guidelines."
        ));
    }
}