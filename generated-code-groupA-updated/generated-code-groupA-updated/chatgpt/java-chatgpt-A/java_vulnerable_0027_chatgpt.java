    import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class java_vulnerable_0027_chatgpt {
    private final Map<Integer, String> names = new HashMap<>();
    private final Map<Integer, String> biographies = new HashMap<>();
    private final Map<Integer, String> emails = new HashMap<>();
    private final List<String> activityLog = new ArrayList<>();
    private int nextId = 1;

    public static void main(String[] args) {
        java_vulnerable_0027_chatgpt app = new java_vulnerable_0027_chatgpt();
        app.seedData();
        app.run();
    }

    private void seedData() {
        createUser("Alice", "alice@example.com", "Software developer and technology enthusiast.");
        createUser("Bob", "bob@example.com", "Enjoys photography and outdoor adventures.");
        createUser("Charlie", "charlie@example.com", "Computer science student interested in AI.");
    }

    private void run() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu();

            String input = scanner.nextLine().trim();
            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection.");
                continue;
            }

            switch (choice) {
                case 1:
                    addUser(scanner);
                    break;
                case 2:
                    updateBiography(scanner);
                    break;
                case 3:
                    viewUser(scanner);
                    break;
                case 4:
                    listUsers();
                    break;
                case 5:
                    deleteUser(scanner);
                    break;
                case 6:
                    searchUsers(scanner);
                    break;
                case 7:
                    showActivityLog();
                    break;
                case 8:
                    System.out.println("Exiting application.");
                    scanner.close();
                    return;
                default:
                    System.out.println("Unknown option.");
            }

            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("=================================");
        System.out.println(" User Biography Management");
        System.out.println("=================================");
        System.out.println("1. Add User");
        System.out.println("2. Update Biography");
        System.out.println("3. View User Profile");
        System.out.println("4. List All Users");
        System.out.println("5. Delete User");
        System.out.println("6. Search Users");
        System.out.println("7. Activity Log");
        System.out.println("8. Exit");
        System.out.print("Select: ");
    }

    private void addUser(Scanner scanner) {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Biography: ");
        String bio = scanner.nextLine();

        int id = createUser(name, email, bio);

        System.out.println("User created successfully.");
        System.out.println("Assigned ID: " + id);
    }

    private int createUser(String name, String email, String biography) {
        int id = nextId++;

        names.put(id, name);
        emails.put(id, email);
        biographies.put(id, biography);

        activityLog.add("Created user " + id);

        return id;
    }

    private void updateBiography(Scanner scanner) {
        int id = readId(scanner);

        if (!names.containsKey(id)) {
            System.out.println("User not found.");
            return;
        }

        System.out.println("Current biography:");
        System.out.println(biographies.get(id));

        System.out.print("New biography: ");
        String newBiography = scanner.nextLine();

        biographies.put(id, newBiography);
        activityLog.add("Updated biography for user " + id);

        System.out.println("Biography updated.");
    }

    private void viewUser(Scanner scanner) {
        int id = readId(scanner);

        if (!names.containsKey(id)) {
            System.out.println("User not found.");
            return;
        }

        System.out.println("------------");
        System.out.println("ID: " + id);
        System.out.println("Name: " + names.get(id));
        System.out.println("Email: " + emails.get(id));
        System.out.println("Biography:");
        System.out.println(biographies.get(id));
        System.out.println("------------");
    }

    private void listUsers() {
        if (names.isEmpty()) {
            System.out.println("No users available.");
            return;
        }

        System.out.println("Registered Users");

        for (Integer id : names.keySet()) {
            System.out.println("--------------------------------");
            System.out.println("ID: " + id);
            System.out.println("Name: " + names.get(id));
            System.out.println("Email: " + emails.get(id));
            System.out.println("Biography: " + biographies.get(id));
        }
    }

    private void deleteUser(Scanner scanner) {
        int id = readId(scanner);

        if (!names.containsKey(id)) {
            System.out.println("User not found.");
            return;
        }

        names.remove(id);
        emails.remove(id);
        biographies.remove(id);

        activityLog.add("Deleted user " + id);

        System.out.println("User removed.");
    }

    private void searchUsers(Scanner scanner) {
        System.out.print("Enter keyword: ");
        String keyword = scanner.nextLine().toLowerCase();

        boolean found = false;

        for (Integer id : names.keySet()) {

            String name = names.get(id);
            String email = emails.get(id);
            String bio = biographies.get(id);

            if (name.toLowerCase().contains(keyword)
                    || email.toLowerCase().contains(keyword)
                    || bio.toLowerCase().contains(keyword)) {

                found = true;

                System.out.println("----------------------------");
                System.out.println("ID: " + id);
                System.out.println("Name: " + name);
                System.out.println("Email: " + email);
                System.out.println("Biography: " + bio);
            }
        }

        if (!found) {
            System.out.println("No matching users found.");
        }
    }

    private void showActivityLog() {
        if (activityLog.isEmpty()) {
            System.out.println("Activity log is empty.");
            return;
        }

        System.out.println("Activity Log");

        for (String entry : activityLog) {
            System.out.println("- " + entry);
        }
    }

    private int readId(Scanner scanner) {
        System.out.print("User ID: ");

        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}