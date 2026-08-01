import java.util.HashMap;
import java.util.Map;

public class Edit_blog_article_022_Secure {

    static Map<Integer, String[]> articles = new HashMap<>();

    static {
        articles.put(1, new String[]{
                "Old Title",
                "Old Content"
        });
    }

    public static String editArticle(
            int articleId,
            String title,
            String content,
            boolean hasPermission) {

        try {

            if (!hasPermission)
                return "Permission denied.";

            if (!articles.containsKey(articleId))
                return "Invalid article ID.";

            title = title == null ? "" : title.trim();
            content = content == null ? "" : content.trim();

            if (title.isEmpty() || content.isEmpty())
                return "Title and content are required.";

            articles.put(articleId,
                    new String[]{title, content});

            return "Article updated successfully.";

        } catch (Exception e) {
            return "Unable to update article.";
        }
    }

    public static void main(String[] args) {

        System.out.println(editArticle(
                1,
                "Secure Title",
                "Secure Content",
                true
        ));
    }
}