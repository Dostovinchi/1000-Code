import java.util.ArrayList;
import java.util.Scanner;

public class java_secure_0029_chatgpt {
    private static final Scanner scanner = new Scanner(System.in);

    private static final ArrayList<Integer> ids = new ArrayList<>();
    private static final ArrayList<String> titles = new ArrayList<>();
    private static final ArrayList<String> authors = new ArrayList<>();
    private static final ArrayList<String> contents = new ArrayList<>();

    private static int nextId = 1;

    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            displayMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    createArticle();
                    break;

                case "2":
                    viewArticles();
                    break;

                case "3":
                    updateArticle();
                    break;

                case "4":
                    deleteArticle();
                    break;

                case "5":
                    searchArticles();
                    break;

                case "6":
                    displayStatistics();
                    break;

                case "7":
                    running = false;
                    System.out.println("Application closed.");
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void displayMenu() {

        System.out.println("\n===== News Article Manager =====");
        System.out.println("1. Create Article");
        System.out.println("2. View Articles");
        System.out.println("3. Update Article");
        System.out.println("4. Delete Article");
        System.out.println("5. Search Articles");
        System.out.println("6. Statistics");
        System.out.println("7. Exit");
        System.out.print("Choose option: ");
    }

    private static void createArticle() {

        System.out.print("Title: ");
        String title = scanner.nextLine().trim();

        if (title.isEmpty()) {
            System.out.println("Title cannot be empty.");
            return;
        }

        System.out.print("Author: ");
        String author = scanner.nextLine().trim();

        if (author.isEmpty()) {
            System.out.println("Author cannot be empty.");
            return;
        }

        System.out.print("Content: ");
        String content = scanner.nextLine().trim();

        if (content.isEmpty()) {
            System.out.println("Content cannot be empty.");
            return;
        }

        ids.add(nextId++);
        titles.add(title);
        authors.add(author);
        contents.add(content);

        System.out.println("Article created successfully.");
    }

    private static void viewArticles() {

        if (ids.isEmpty()) {
            System.out.println("No articles available.");
            return;
        }

        for (int i = 0; i < ids.size(); i++) {

            System.out.println("----------------------------");
            System.out.println("ID: " + ids.get(i));
            System.out.println("Title: " + titles.get(i));
            System.out.println("Author: " + authors.get(i));
            System.out.println("Content: " + contents.get(i));
        }
    }

    private static void updateArticle() {

        int index = readArticleIndex();

        if (index == -1) {
            return;
        }

        System.out.print("New title: ");
        String title = scanner.nextLine().trim();

        if (!title.isEmpty()) {
            titles.set(index, title);
        }

        System.out.print("New author: ");
        String author = scanner.nextLine().trim();

        if (!author.isEmpty()) {
            authors.set(index, author);
        }

        System.out.print("New content: ");
        String content = scanner.nextLine().trim();

        if (!content.isEmpty()) {
            contents.set(index, content);
        }

        System.out.println("Article updated successfully.");
    }

    private static void deleteArticle() {

        int index = readArticleIndex();

        if (index == -1) {
            return;
        }

        ids.remove(index);
        titles.remove(index);
        authors.remove(index);
        contents.remove(index);

        System.out.println("Article deleted successfully.");
    }

    private static void searchArticles() {

        System.out.print("Enter keyword: ");
        String keyword = scanner.nextLine().trim().toLowerCase();

        boolean found = false;

        for (int i = 0; i < ids.size(); i++) {

            if (titles.get(i).toLowerCase().contains(keyword)
                    || authors.get(i).toLowerCase().contains(keyword)
                    || contents.get(i).toLowerCase().contains(keyword)) {

                System.out.println("----------------------------");
                System.out.println("ID: " + ids.get(i));
                System.out.println("Title: " + titles.get(i));
                System.out.println("Author: " + authors.get(i));

                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching articles found.");
        }
    }

    private static void displayStatistics() {

        System.out.println("\nStatistics");
        System.out.println("----------------");
        System.out.println("Total articles: " + ids.size());

        int totalCharacters = 0;

        for (String content : contents) {
            totalCharacters += content.length();
        }

        System.out.println("Total content characters: " + totalCharacters);
    }

    private static int readArticleIndex() {

        System.out.print("Article ID: ");

        int id;

        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return -1;
        }

        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) == id) {
                return i;
            }
        }

        System.out.println("Article not found.");
        return -1;
    }
}

