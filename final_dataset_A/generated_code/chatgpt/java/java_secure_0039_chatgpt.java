import java.util.ArrayList;
import java.util.Scanner;

public class java_secure_0039_chatgpt {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ArrayList<String> files = new ArrayList<>();

    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            displayMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    addFile();
                    break;

                case "2":
                    viewFiles();
                    break;

                case "3":
                    openFile();
                    break;

                case "4":
                    renameFile();
                    break;

                case "5":
                    deleteFile();
                    break;

                case "6":
                    searchFiles();
                    break;

                case "7":
                    showStatistics();
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

        System.out.println("\n===== Documents Folder Browser =====");
        System.out.println("1. Add File");
        System.out.println("2. View Files");
        System.out.println("3. Open File");
        System.out.println("4. Rename File");
        System.out.println("5. Delete File");
        System.out.println("6. Search Files");
        System.out.println("7. Statistics");
        System.out.println("8. Exit");
        System.out.print("Choose option: ");
    }

    private static void addFile() {

        System.out.print("Enter file name: ");
        String fileName = scanner.nextLine().trim();

        if (fileName.isEmpty()) {
            System.out.println("File name cannot be empty.");
            return;
        }

        if (findFile(fileName) != -1) {
            System.out.println("File already exists.");
            return;
        }

        files.add(fileName);

        System.out.println("File added successfully.");
    }

    private static void viewFiles() {

        if (files.isEmpty()) {
            System.out.println("No files available.");
            return;
        }

        System.out.println("\n--- Documents Folder ---");

        for (int i = 0; i < files.size(); i++) {
            System.out.println((i + 1) + ". " + files.get(i));
        }
    }

    private static void openFile() {

        System.out.print("Enter file name: ");
        String fileName = scanner.nextLine().trim();

        int index = findFile(fileName);

        if (index == -1) {
            System.out.println("File not found.");
            return;
        }

        System.out.println("Opening file: " + files.get(index));
    }

    private static void renameFile() {

        System.out.print("Enter current file name: ");
        String currentName = scanner.nextLine().trim();

        int index = findFile(currentName);

        if (index == -1) {
            System.out.println("File not found.");
            return;
        }

        System.out.print("Enter new file name: ");
        String newName = scanner.nextLine().trim();

        if (newName.isEmpty()) {
            System.out.println("New file name cannot be empty.");
            return;
        }

        files.set(index, newName);

        System.out.println("File renamed successfully.");
    }

    private static void deleteFile() {

        System.out.print("Enter file name: ");
        String fileName = scanner.nextLine().trim();

        int index = findFile(fileName);

        if (index == -1) {
            System.out.println("File not found.");
            return;
        }

        files.remove(index);

        System.out.println("File deleted successfully.");
    }

    private static void searchFiles() {

        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine().trim().toLowerCase();

        boolean found = false;

        for (String file : files) {

            if (file.toLowerCase().contains(keyword)) {

                System.out.println(file);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching files found.");
        }
    }

    private static void showStatistics() {

        System.out.println("\n--- Statistics ---");
        System.out.println("Total Files: " + files.size());
    }

    private static int findFile(String fileName) {

        for (int i = 0; i < files.size(); i++) {

            if (files.get(i).equalsIgnoreCase(fileName)) {
                return i;
            }
        }

        return -1;
    }

}
