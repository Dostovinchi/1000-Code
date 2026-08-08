  import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class java_vulnerable_0030_chatgpt {
    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> titles = new ArrayList<>();
    static ArrayList<String> categories = new ArrayList<>();
    static ArrayList<String> descriptions = new ArrayList<>();

    public static void main(String[] args) {

        loadSampleData();

        boolean running = true;

        while (running) {

            printMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    searchRecords();
                    break;

                case "2":
                    listAllRecords();
                    break;

                case "3":
                    addRecord();
                    break;

                case "4":
                    running = false;
                    System.out.println("Application closed.");
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    static void printMenu() {

        System.out.println();
        System.out.println("=================================");
        System.out.println(" Search Results Application");
        System.out.println("=================================");
        System.out.println("1. Search");
        System.out.println("2. Display All Records");
        System.out.println("3. Add Record");
        System.out.println("4. Exit");
        System.out.print("Choice: ");
    }

    static void loadSampleData() {

        addSample(
                "Java Programming",
                "Programming",
                "Introduction to Java programming language."
        );

        addSample(
                "Python Basics",
                "Programming",
                "Learn Python fundamentals."
        );

        addSample(
                "Cybersecurity Essentials",
                "Security",
                "Overview of modern cybersecurity concepts."
        );

        addSample(
                "Database Systems",
                "Database",
                "Introduction to SQL and relational databases."
        );

        addSample(
                "Machine Learning",
                "Artificial Intelligence",
                "Basic machine learning techniques and algorithms."
        );
    }

    static void addSample(String title, String category, String description) {

        titles.add(title);
        categories.add(category);
        descriptions.add(description);
    }

    static void addRecord() {

        System.out.print("Title: ");
        String title = scanner.nextLine().trim();

        if (title.isEmpty()) {
            System.out.println("Title cannot be empty.");
            return;
        }

        System.out.print("Category: ");
        String category = scanner.nextLine().trim();

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        titles.add(title);
        categories.add(category);
        descriptions.add(description);

        System.out.println("Record added successfully.");
    }

    static void searchRecords() {

        System.out.print("Enter search query: ");
        String query = scanner.nextLine().trim().toLowerCase();

        if (query.isEmpty()) {
            System.out.println("Search query cannot be empty.");
            return;
        }

        List<Integer> matches = new ArrayList<>();

        for (int i = 0; i < titles.size(); i++) {

            if (titles.get(i).toLowerCase().contains(query)
                    || categories.get(i).toLowerCase().contains(query)
                    || descriptions.get(i).toLowerCase().contains(query)) {

                matches.add(i);
            }
        }

        if (matches.isEmpty()) {
            System.out.println("No matching records found.");
            return;
        }

        System.out.println();
        System.out.println("Search Results");
        System.out.println("-----------------------------");

        for (int index : matches) {

            System.out.println("Title: " + titles.get(index));
            System.out.println("Category: " + categories.get(index));
            System.out.println("Description: " + descriptions.get(index));
            System.out.println("-----------------------------");
        }

        System.out.println("Total Results: " + matches.size());
    }

    static void listAllRecords() {

        if (titles.isEmpty()) {
            System.out.println("No records available.");
            return;
        }

        System.out.println();

        for (int i = 0; i < titles.size(); i++) {

            System.out.println("-----------------------------");
            System.out.println("Record #" + (i + 1));
            System.out.println("Title: " + titles.get(i));
            System.out.println("Category: " + categories.get(i));
            System.out.println("Description: " + descriptions.get(i));
        }
    }

}
