import java.util.HashMap;
import java.util.Map;

public class Edit_blog_article_022_detelid {

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
            String content) {

        if (!articles.containsKey(articleId))
            return "Article ID not found.";

        articles.put(articleId,
                new String[]{title, content});

        return "Article updated successfully.";
    }

    public static void main(String[] args) {

        System.out.println(editArticle(
                1,
                "New Title",
                "Updated Content"
        ));
    }
}