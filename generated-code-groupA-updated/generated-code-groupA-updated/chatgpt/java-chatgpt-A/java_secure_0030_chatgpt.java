 import java.util.ArrayList;
import java.util.Scanner;

public class java_secure_0030_chatgpt {

    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> titles = new ArrayList<>();
    static ArrayList<String> categories = new ArrayList<>();
    static ArrayList<String> descriptions = new ArrayList<>();

    public static void main(String[] args) {

        loadSampleData();

        boolean running = true;

        while (running) {

            displayMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    searchRecords();
                    break;

                case "2":
                    displayAllRecords();
                    break;

                case "3":
                    addRecord();
                    break;

                case "4":
                    removeRecord();
                    break;

                case "5":
                    displayStatistics();
                    break;

                case "6":
                    running = false;
                    System.out.println("Application closed.");
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    static void displayMenu() {

        System.out.println();
        System.out.println("=================================");
        System.out.println(" Search Results Application");
        System.out.println("=================================");
        System.out.println("1. Search");
        System.out.println("2. Display All Records");
        System.out.println("3. Add Record");
        System.out.println("4. Remove Record");
        System.out.println("5. Statistics");
        System.out.println("6. Exit");
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
                "Learn Python fundamentals and syntax."
        );

        addSample(
                "Cybersecurity Essentials",
                "Security",
                "Basic concepts of information security."
        );

        addSample(
                "Database Systems",
                "Database",
                "SQL and relational database concepts."
        );

        addSample(
                "Machine Learning",
                "Artificial Intelligence",
                "Introduction to machine learning algorithms."
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

        if (category.isEmpty()) {
            System.out.println("Category cannot be empty.");
            return;
        }

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

        int matches = 0;

        System.out.println();
        System.out.println("Search Results");
        System.out.println("-----------------------------");

        for (int i = 0; i < titles.size(); i++) {

            if (titles.get(i).toLowerCase().contains(query)
                    || categories.get(i).toLowerCase().contains(query)
                    || descriptions.get(i).toLowerCase().contains(query)) {

                System.out.println("Record #" + (i + 1));
                System.out.println("Title: " + titles.get(i));
                System.out.println("Category: " + categories.get(i));
                System.out.println("Description: " + descriptions.get(i));
                System.out.println("-----------------------------");

                matches++;
            }
        }

        if (matches == 0) {
            System.out.println("No matching records found.");
        } else {
            System.out.println("Total Matches: " + matches);
        }
    }

    static void displayAllRecords() {

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

    static void removeRecord() {

        System.out.print("Enter record title: ");
        String title = scanner.nextLine().trim();

        for (int i = 0; i < titles.size(); i++) {

            if (titles.get(i).equalsIgnoreCase(title)) {

                titles.remove(i);
                categories.remove(i);
                descriptions.remove(i);

                System.out.println("Record removed successfully.");
                return;
            }
        }

        System.out.println("Record not found.");
    }

    static void displayStatistics() {

        System.out.println();
        System.out.println("Statistics");
        System.out.println("-----------------------------");
        System.out.println("Total Records: " + titles.size());

        int programming = 0;
        int security = 0;
        int database = 0;
        int other = 0;

        for (String category : categories) {

            if (category.equalsIgnoreCase("Programming")) {
                programming++;
            } else if (category.equalsIgnoreCase("Security")) {
                security++;
            } else if (category.equalsIgnoreCase("Database")) {
                database++;
            } else {
                other++;
            }
        }

        System.out.println("Programming: " + programming);
        System.out.println("Security: " + security);
        System.out.println("Database: " + database);
        System.out.println("Other: " + other);
    }

    
}
