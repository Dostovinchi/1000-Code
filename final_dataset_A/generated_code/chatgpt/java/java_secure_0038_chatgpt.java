import java.util.ArrayList;
import java.util.Scanner;

public class java_secure_0038_chatgpt {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ArrayList<String> logNames = new ArrayList<>();
    private static final ArrayList<String> logContents = new ArrayList<>();

    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            displayMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addLog();
                    break;
                case "2":
                    viewLogs();
                    break;
                case "3":
                    readLog();
                    break;
                case "4":
                    updateLog();
                    break;
                case "5":
                    deleteLog();
                    break;
                case "6":
                    searchLogs();
                    break;
                case "7":
                    displayStatistics();
                    break;
                case "8":
                    running = false;
                    System.out.println("Application closed.");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void displayMenu() {
        System.out.println("\n===== Server Log Viewer =====");
        System.out.println("1. Add Log File");
        System.out.println("2. View Log Files");
        System.out.println("3. Read Log File");
        System.out.println("4. Update Log File");
        System.out.println("5. Delete Log File");
        System.out.println("6. Search Logs");
        System.out.println("7. Statistics");
        System.out.println("8. Exit");
        System.out.print("Choose option: ");
    }

    private static void addLog() {

        System.out.print("Enter log file name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Log file name cannot be empty.");
            return;
        }

        if (findLog(name) != -1) {
            System.out.println("Log file already exists.");
            return;
        }

        System.out.print("Enter log content: ");
        String content = scanner.nextLine();

        logNames.add(name);
        logContents.add(content);

        System.out.println("Log file added successfully.");
    }

    private static void viewLogs() {

        if (logNames.isEmpty()) {
            System.out.println("No log files available.");
            return;
        }

        System.out.println("\n--- Log Files ---");

        for (int i = 0; i < logNames.size(); i++) {
            System.out.println((i + 1) + ". " + logNames.get(i));
        }
    }

    private static void readLog() {

        System.out.print("Enter log file name: ");
        String name = scanner.nextLine().trim();

        int index = findLog(name);

        if (index == -1) {
            System.out.println("Log file not found.");
            return;
        }

        System.out.println("\n--- Log Content ---");
        System.out.println(logContents.get(index));
    }

    private static void updateLog() {

        System.out.print("Enter log file name: ");
        String name = scanner.nextLine().trim();

        int index = findLog(name);

        if (index == -1) {
            System.out.println("Log file not found.");
            return;
        }

        System.out.print("Enter new log content: ");
        String content = scanner.nextLine();

        logContents.set(index, content);

        System.out.println("Log updated successfully.");
    }

    private static void deleteLog() {

        System.out.print("Enter log file name: ");
        String name = scanner.nextLine().trim();

        int index = findLog(name);

        if (index == -1) {
            System.out.println("Log file not found.");
            return;
        }

        logNames.remove(index);
        logContents.remove(index);

        System.out.println("Log deleted successfully.");
    }

    private static void searchLogs() {

        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine().trim().toLowerCase();

        boolean found = false;

        for (int i = 0; i < logNames.size(); i++) {

            if (logNames.get(i).toLowerCase().contains(keyword)
                    || logContents.get(i).toLowerCase().contains(keyword)) {

                System.out.println("----------------------------");
                System.out.println("File: " + logNames.get(i));
                System.out.println("Content: " + logContents.get(i));

                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching log files found.");
        }
    }

    private static void displayStatistics() {

        System.out.println("\n--- Statistics ---");
        System.out.println("Total Log Files: " + logNames.size());
    }

    private static int findLog(String name) {

        for (int i = 0; i < logNames.size(); i++) {

            if (logNames.get(i).equalsIgnoreCase(name)) {
                return i;
            }
        }

        return -1;
    }

}
