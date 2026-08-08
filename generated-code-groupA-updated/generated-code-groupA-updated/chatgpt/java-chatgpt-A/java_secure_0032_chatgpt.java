 import java.util.ArrayList;
import java.util.Scanner;

public class java_secure_0032_chatgpt {
    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> usernames = new ArrayList<>();
    static ArrayList<String> fullNames = new ArrayList<>();
    static ArrayList<String> emails = new ArrayList<>();
    static ArrayList<String> descriptions = new ArrayList<>();

    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            displayMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    addUser();
                    break;

                case "2":
                    displayProfiles();
                    break;

                case "3":
                    updateDescription();
                    break;

                case "4":
                    retrieveProfile();
                    break;

                case "5":
                    searchUser();
                    break;

                case "6":
                    deleteUser();
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


    static void displayMenu() {

        System.out.println("\n===== User Profile Management System =====");
        System.out.println("1. Add User");
        System.out.println("2. Display All Profiles");
        System.out.println("3. Update Profile Description");
        System.out.println("4. Retrieve Profile Information");
        System.out.println("5. Search User");
        System.out.println("6. Delete User");
        System.out.println("7. Exit");
        System.out.print("Choose option: ");
    }


    static void addUser() {

        System.out.println("\n--- Add New User ---");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        if (usernames.contains(username)) {
            System.out.println("Username already exists.");
            return;
        }

        System.out.print("Full name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Profile description: ");
        String description = scanner.nextLine().trim();


        usernames.add(username);
        fullNames.add(name);
        emails.add(email);
        descriptions.add(description);


        System.out.println("User profile created successfully.");
    }


    static void displayProfiles() {

        if (usernames.isEmpty()) {
            System.out.println("No user profiles available.");
            return;
        }


        System.out.println("\n--- All Profiles ---");


        for (int i = 0; i < usernames.size(); i++) {

            printProfile(i);
        }
    }


    static void updateDescription() {

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();


        int index = findUser(username);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        System.out.print("New description: ");
        String description = scanner.nextLine().trim();


        if (!description.isEmpty()) {

            descriptions.set(index, description);

            System.out.println("Profile description updated.");
        }
        else {

            System.out.println("Description cannot be empty.");
        }
    }


    static void retrieveProfile() {

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();


        int index = findUser(username);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        printProfile(index);
    }


    static void searchUser() {

        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine().toLowerCase().trim();


        boolean found = false;


        for (int i = 0; i < usernames.size(); i++) {


            if (usernames.get(i).toLowerCase().contains(keyword)
                    || fullNames.get(i).toLowerCase().contains(keyword)
                    || descriptions.get(i).toLowerCase().contains(keyword)) {


                printProfile(i);

                found = true;
            }
        }


        if (!found) {

            System.out.println("No matching users found.");
        }
    }


    static void deleteUser() {

        System.out.print("Enter username to delete: ");
        String username = scanner.nextLine().trim();


        int index = findUser(username);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        usernames.remove(index);
        fullNames.remove(index);
        emails.remove(index);
        descriptions.remove(index);


        System.out.println("User deleted successfully.");
    }


    static int findUser(String username) {

        for (int i = 0; i < usernames.size(); i++) {

            if (usernames.get(i).equalsIgnoreCase(username)) {

                return i;
            }
        }

        return -1;
    }


    static void printProfile(int index) {

        System.out.println("\n-----------------------");
        System.out.println("Username: " + usernames.get(index));
        System.out.println("Name: " + fullNames.get(index));
        System.out.println("Email: " + emails.get(index));
        System.out.println("Description: " + descriptions.get(index));
        System.out.println("-----------------------");
    }

    
}
