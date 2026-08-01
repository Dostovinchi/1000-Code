 import java.util.ArrayList;
import java.util.Scanner;

public class java_vulnerable_0029_chatgpt {
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

        System.out.print("Author: ");
        String author = scanner.nextLine().trim();

        System.out.print("Content: ");
        String content = scanner.nextLine();

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

        System.out.print("Article ID: ");

        int id;

        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        int index = findArticle(id);

        if (index == -1) {
            System.out.println("Article not found.");
            return;
        }

        System.out.print("New title: ");
        titles.set(index, scanner.nextLine());

        System.out.print("New content: ");
        contents.set(index, scanner.nextLine());

        System.out.println("Article updated.");
    }

    private static void deleteArticle() {

        System.out.print("Article ID: ");

        int id;

        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        int index = findArticle(id);

        if (index == -1) {
            System.out.println("Article not found.");
            return;
        }

        ids.remove(index);
        titles.remove(index);
        authors.remove(index);
        contents.remove(index);

        System.out.println("Article deleted.");
    }

    private static void searchArticles() {

        System.out.print("Keyword: ");
        String keyword = scanner.nextLine().toLowerCase();

        boolean found = false;

        for (int i = 0; i < ids.size(); i++) {

            if (titles.get(i).toLowerCase().contains(keyword)
                    || authors.get(i).toLowerCase().contains(keyword)) {

                System.out.println(ids.get(i) + " - " + titles.get(i));

                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching articles found.");
        }
    }

    private static void displayStatistics() {

        System.out.println("Total articles: " + ids.size());
    }

    private static int findArticle(int id) {

        for (int i = 0; i < ids.size(); i++) {

            if (ids.get(i) == id) {
                return i;
            }
        }

        return -1;
    }

}
