import java.util.ArrayList;
import java.util.Scanner;

public class java_vulnerable_0039_chatgpt {
    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> documentFiles = new ArrayList<>();

    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            showMenu();

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
                    running = false;
                    System.out.println("Application closed.");
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    static void showMenu() {

        System.out.println("\n===== Documents Folder Browser =====");
        System.out.println("1. Add File");
        System.out.println("2. View Files");
        System.out.println("3. Open File");
        System.out.println("4. Rename File");
        System.out.println("5. Delete File");
        System.out.println("6. Search Files");
        System.out.println("7. Exit");
        System.out.print("Choose option: ");
    }

    static void addFile() {

        System.out.print("Enter file name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("File name cannot be empty.");
            return;
        }

        if (findFile(name) != -1) {
            System.out.println("File already exists.");
            return;
        }

        documentFiles.add(name);

        System.out.println("File added successfully.");
    }

    static void viewFiles() {

        if (documentFiles.isEmpty()) {
            System.out.println("No files available.");
            return;
        }

        System.out.println("\n--- Documents Folder ---");

        for (int i = 0; i < documentFiles.size(); i++) {
            System.out.println((i + 1) + ". " + documentFiles.get(i));
        }
    }

    static void openFile() {

        System.out.print("Enter file name: ");
        String name = scanner.nextLine().trim();

        int index = findFile(name);

        if (index == -1) {
            System.out.println("File not found.");
            return;
        }

        System.out.println("Opening: " + documentFiles.get(index));
    }

    static void renameFile() {

        System.out.print("Enter current file name: ");
        String current = scanner.nextLine().trim();

        int index = findFile(current);

        if (index == -1) {
            System.out.println("File not found.");
            return;
        }

        System.out.print("Enter new file name: ");
        String updated = scanner.nextLine().trim();

        documentFiles.set(index, updated);

        System.out.println("File renamed successfully.");
    }

    static void deleteFile() {

        System.out.print("Enter file name: ");
        String name = scanner.nextLine().trim();

        int index = findFile(name);

        if (index == -1) {
            System.out.println("File not found.");
            return;
        }

        documentFiles.remove(index);

        System.out.println("File deleted successfully.");
    }

    static void searchFiles() {

        System.out.print("Enter keyword: ");
        String keyword = scanner.nextLine().trim().toLowerCase();

        boolean found = false;

        for (String file : documentFiles) {

            if (file.toLowerCase().contains(keyword)) {
                System.out.println(file);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching files found.");
        }
    }

    static int findFile(String name) {

        for (int i = 0; i < documentFiles.size(); i++) {

            if (documentFiles.get(i).equalsIgnoreCase(name)) {
                return i;
            }
        }

        return -1;
    }
 
}
