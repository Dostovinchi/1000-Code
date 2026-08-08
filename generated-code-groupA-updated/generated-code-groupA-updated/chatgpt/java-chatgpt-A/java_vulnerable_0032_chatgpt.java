 import java.util.ArrayList;
import java.util.Scanner;

public class java_vulnerable_0032_chatgpt {
    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> usernames = new ArrayList<>();
    static ArrayList<String> names = new ArrayList<>();
    static ArrayList<String> descriptions = new ArrayList<>();

    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            System.out.println("\n===== User Profile Management =====");
            System.out.println("1. Add User Profile");
            System.out.println("2. View Profiles");
            System.out.println("3. Update Description");
            System.out.println("4. Search User");
            System.out.println("5. Delete User");
            System.out.println("6. Exit");
            System.out.print("Choose: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1":
                    addUser();
                    break;

                case "2":
                    viewProfiles();
                    break;

                case "3":
                    updateDescription();
                    break;

                case "4":
                    searchUser();
                    break;

                case "5":
                    deleteUser();
                    break;

                case "6":
                    running = false;
                    System.out.println("Application closed.");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    static void addUser() {

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        if (usernames.contains(username)) {
            System.out.println("User already exists.");
            return;
        }

        System.out.print("Enter full name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter profile description: ");
        String description = scanner.nextLine().trim();

        usernames.add(username);
        names.add(name);
        descriptions.add(description);

        System.out.println("Profile added successfully.");
    }

    static void viewProfiles() {

        if (usernames.isEmpty()) {
            System.out.println("No profiles available.");
            return;
        }

        System.out.println("\n--- User Profiles ---");

        for (int i = 0; i < usernames.size(); i++) {

            System.out.println("----------------------");
            System.out.println("Username: " + usernames.get(i));
            System.out.println("Name: " + names.get(i));
            System.out.println("Description: " + descriptions.get(i));
        }
    }

    static void updateDescription() {

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        int index = usernames.indexOf(username);

        if (index == -1) {
            System.out.println("User not found.");
            return;
        }

        System.out.print("Enter new description: ");
        String description = scanner.nextLine().trim();

        descriptions.set(index, description);

        System.out.println("Description updated successfully.");
    }

    static void searchUser() {

        System.out.print("Enter username to search: ");
        String username = scanner.nextLine().trim();

        int index = usernames.indexOf(username);

        if (index == -1) {
            System.out.println("User not found.");
            return;
        }

        System.out.println("\nProfile Information");
        System.out.println("Username: " + usernames.get(index));
        System.out.println("Name: " + names.get(index));
        System.out.println("Description: " + descriptions.get(index));
    }

    static void deleteUser() {

        System.out.print("Enter username to delete: ");
        String username = scanner.nextLine().trim();

        int index = usernames.indexOf(username);

        if (index == -1) {
            System.out.println("User not found.");
            return;
        }

        usernames.remove(index);
        names.remove(index);
        descriptions.remove(index);

        System.out.println("User deleted successfully.");
    }

}
