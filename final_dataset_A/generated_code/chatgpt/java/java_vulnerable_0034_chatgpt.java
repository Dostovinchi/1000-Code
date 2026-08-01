import java.util.ArrayList;
import java.util.Scanner;

public class java_vulnerable_0034_chatgpt {
    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> usernames = new ArrayList<>();
    static ArrayList<String> profilePictures = new ArrayList<>();


    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            showMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    createUser();
                    break;

                case "2":
                    viewProfiles();
                    break;

                case "3":
                    uploadPicture();
                    break;

                case "4":
                    replacePicture();
                    break;

                case "5":
                    removePicture();
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


    static void showMenu() {

        System.out.println("\n===== Profile Picture Management =====");
        System.out.println("1. Create User");
        System.out.println("2. View User Profiles");
        System.out.println("3. Upload Profile Picture");
        System.out.println("4. Replace Profile Picture");
        System.out.println("5. Remove Profile Picture");
        System.out.println("6. Delete User");
        System.out.println("7. Exit");
        System.out.print("Choose option: ");
    }


    static void createUser() {

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


        usernames.add(username);
        profilePictures.add("No picture uploaded");


        System.out.println("User created successfully.");
    }


    static void viewProfiles() {

        if (usernames.isEmpty()) {

            System.out.println("No users available.");
            return;
        }


        System.out.println("\n--- User Profiles ---");


        for (int i = 0; i < usernames.size(); i++) {

            System.out.println("----------------------");
            System.out.println("Username: " + usernames.get(i));
            System.out.println("Profile Picture: " + profilePictures.get(i));
        }
    }


    static int findUser(String username) {

        for (int i = 0; i < usernames.size(); i++) {

            if (usernames.get(i).equalsIgnoreCase(username)) {

                return i;
            }
        }

        return -1;
    }


    static void uploadPicture() {

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();


        int index = findUser(username);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        if (!profilePictures.get(index).equals("No picture uploaded")) {

            System.out.println("Picture already exists. Use replace option.");
            return;
        }


        System.out.print("Enter picture file name: ");
        String picture = scanner.nextLine().trim();


        if (picture.isEmpty()) {

            System.out.println("Invalid picture name.");
            return;
        }


        profilePictures.set(index, picture);


        System.out.println("Picture uploaded successfully.");
    }


    static void replacePicture() {

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();


        int index = findUser(username);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        System.out.print("Enter new picture file name: ");

        String picture = scanner.nextLine().trim();


        if (picture.isEmpty()) {

            System.out.println("Invalid picture name.");
            return;
        }


        profilePictures.set(index, picture);


        System.out.println("Picture replaced successfully.");
    }


    static void removePicture() {

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();


        int index = findUser(username);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        profilePictures.set(index, "No picture uploaded");


        System.out.println("Picture removed successfully.");
    }


    static void deleteUser() {

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();


        int index = findUser(username);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        usernames.remove(index);
        profilePictures.remove(index);


        System.out.println("User deleted successfully.");
    }

}
