import java.util.ArrayList;
import java.util.Scanner;

public class java_Secure_0027_chatgpt {
   
    static Scanner scanner = new Scanner(System.in);

    static ArrayList<Integer> ids = new ArrayList<>();
    static ArrayList<String> usernames = new ArrayList<>();
    static ArrayList<String> fullNames = new ArrayList<>();
    static ArrayList<String> emails = new ArrayList<>();
    static ArrayList<String> biographies = new ArrayList<>();
    static ArrayList<String> createdDates = new ArrayList<>();

    static int nextId = 1;

    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            printMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    addUser();
                    break;

                case "2":
                    updateBiography();
                    break;

                case "3":
                    viewProfile();
                    break;

                case "4":
                    listProfiles();
                    break;

                case "5":
                    searchProfile();
                    break;

                case "6":
                    deleteProfile();
                    break;

                case "7":
                    updateProfileInformation();
                    break;

                case "8":
                    statistics();
                    break;

                case "9":
                    running = false;
                    System.out.println("Application closed.");
                    break;

                default:
                    System.out.println("Invalid selection.");
            }
        }
    }


    static void printMenu() {

        System.out.println();
        System.out.println("==============================");
        System.out.println(" User Profile Biography Manager");
        System.out.println("==============================");
        System.out.println("1. Add User");
        System.out.println("2. Update Biography");
        System.out.println("3. View User");
        System.out.println("4. List Users");
        System.out.println("5. Search User");
        System.out.println("6. Delete User");
        System.out.println("7. Update User Information");
        System.out.println("8. Statistics");
        System.out.println("9. Exit");
        System.out.print("Choice: ");
    }


    static void addUser() {

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        if (findUserByUsername(username) != -1) {
            System.out.println("Username already exists.");
            return;
        }


        System.out.print("Full name: ");
        String fullName = scanner.nextLine().trim();

        if (fullName.isEmpty()) {
            System.out.println("Full name cannot be empty.");
            return;
        }


        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        if (!email.contains("@") || !email.contains(".")) {
            System.out.println("Invalid email.");
            return;
        }


        System.out.print("Biography: ");
        String biography = scanner.nextLine();


        ids.add(nextId++);
        usernames.add(username);
        fullNames.add(fullName);
        emails.add(email);
        biographies.add(biography);
        createdDates.add(java.time.LocalDate.now().toString());


        System.out.println("User created successfully.");
    }


    static void updateBiography() {

        System.out.print("Username: ");
        String username = scanner.nextLine();

        int index = findUserByUsername(username);


        if (index == -1) {
            System.out.println("User not found.");
            return;
        }


        System.out.println("Current biography:");
        System.out.println(biographies.get(index));


        System.out.print("New biography: ");
        String bio = scanner.nextLine();


        biographies.set(index, bio);

        System.out.println("Biography updated.");
    }


    static void viewProfile() {

        System.out.print("Username: ");
        String username = scanner.nextLine();


        int index = findUserByUsername(username);


        if (index == -1) {
            System.out.println("User not found.");
            return;
        }


        System.out.println("----------------------------");
        System.out.println("ID: " + ids.get(index));
        System.out.println("Username: " + usernames.get(index));
        System.out.println("Full Name: " + fullNames.get(index));
        System.out.println("Email: " + emails.get(index));
        System.out.println("Biography: " + biographies.get(index));
        System.out.println("Created: " + createdDates.get(index));
    }


    static void listProfiles() {

        if (usernames.isEmpty()) {
            System.out.println("No users available.");
            return;
        }


        for (int i = 0; i < usernames.size(); i++) {

            System.out.println("----------------------------");
            System.out.println("ID: " + ids.get(i));
            System.out.println("Username: " + usernames.get(i));
            System.out.println("Name: " + fullNames.get(i));
            System.out.println("Email: " + emails.get(i));
            System.out.println("Biography: " + biographies.get(i));
        }
    }


    static void searchProfile() {

        System.out.print("Enter username or name: ");
        String keyword = scanner.nextLine().toLowerCase();


        boolean found = false;


        for (int i = 0; i < usernames.size(); i++) {


            if (usernames.get(i).toLowerCase().contains(keyword)
                    || fullNames.get(i).toLowerCase().contains(keyword)) {


                System.out.println("----------------------------");
                System.out.println("Username: " + usernames.get(i));
                System.out.println("Name: " + fullNames.get(i));
                System.out.println("Email: " + emails.get(i));

                found = true;
            }
        }


        if (!found) {
            System.out.println("No matching users found.");
        }
    }


    static void deleteProfile() {

        System.out.print("Username: ");
        String username = scanner.nextLine();


        int index = findUserByUsername(username);


        if (index == -1) {
            System.out.println("User not found.");
            return;
        }


        ids.remove(index);
        usernames.remove(index);
        fullNames.remove(index);
        emails.remove(index);
        biographies.remove(index);
        createdDates.remove(index);


        System.out.println("User deleted successfully.");
    }


    static void updateProfileInformation() {

        System.out.print("Username: ");
        String username = scanner.nextLine();


        int index = findUserByUsername(username);


        if (index == -1) {
            System.out.println("User not found.");
            return;
        }


        System.out.print("New full name: ");
        String name = scanner.nextLine();


        if (!name.isEmpty()) {
            fullNames.set(index, name);
        }


        System.out.print("New email: ");
        String email = scanner.nextLine();


        if (!email.isEmpty()
                && email.contains("@")
                && email.contains(".")) {

            emails.set(index, email);
        }


        System.out.println("Profile updated.");
    }


    static void statistics() {

        System.out.println("Total users: " + usernames.size());


        int withBiography = 0;
        int longest = 0;


        for (String bio : biographies) {


            if (!bio.trim().isEmpty()) {
                withBiography++;
            }


            if (bio.length() > longest) {
                longest = bio.length();
            }
        }


        System.out.println("Users with biography: " + withBiography);
        System.out.println("Users without biography: "
                + (usernames.size() - withBiography));

        System.out.println("Longest biography length: "
                + longest + " characters");
    }


    static int findUserByUsername(String username) {

        for (int i = 0; i < usernames.size(); i++) {

            if (usernames.get(i).equalsIgnoreCase(username)) {
                return i;
            }
        }

        return -1;
    }

    
}
