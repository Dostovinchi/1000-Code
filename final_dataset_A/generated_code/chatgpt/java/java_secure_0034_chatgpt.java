import java.util.ArrayList;
import java.util.Scanner;

public class java_secure_0034_chatgpt {
    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> usernames = new ArrayList<>();
    static ArrayList<String> pictureNames = new ArrayList<>();


    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            showMenu();

            String choice = scanner.nextLine().trim();


            switch (choice) {

                case "1":
                    addUser();
                    break;

                case "2":
                    viewUsers();
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
                    searchUser();
                    break;

                case "7":
                    deleteUser();
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


    static void showMenu() {

        System.out.println("\n===== Profile Picture Management =====");
        System.out.println("1. Add User");
        System.out.println("2. View Users");
        System.out.println("3. Upload Picture");
        System.out.println("4. Replace Picture");
        System.out.println("5. Remove Picture");
        System.out.println("6. Search User");
        System.out.println("7. Delete User");
        System.out.println("8. Exit");
        System.out.print("Choose option: ");
    }


    static void addUser() {

        System.out.print("Username: ");

        String username = scanner.nextLine().trim();


        if (username.isEmpty()) {

            System.out.println("Username cannot be empty.");
            return;
        }


        if (findUser(username) != -1) {

            System.out.println("User already exists.");
            return;
        }


        usernames.add(username);
        pictureNames.add("No picture");


        System.out.println("User added successfully.");
    }


    static void viewUsers() {

        if (usernames.isEmpty()) {

            System.out.println("No users available.");
            return;
        }


        System.out.println("\n--- User Records ---");


        for (int i = 0; i < usernames.size(); i++) {

            System.out.println("----------------------");
            System.out.println("Username: " + usernames.get(i));
            System.out.println("Picture: " + pictureNames.get(i));
        }
    }


    static void uploadPicture() {

        System.out.print("Username: ");

        String username = scanner.nextLine().trim();


        int index = findUser(username);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        if (!pictureNames.get(index).equals("No picture")) {

            System.out.println("Picture already exists. Use replace option.");
            return;
        }


        System.out.print("Picture file name: ");

        String picture = scanner.nextLine().trim();


        if (picture.isEmpty()) {

            System.out.println("Invalid picture name.");
            return;
        }


        pictureNames.set(index, picture);


        System.out.println("Picture uploaded successfully.");
    }


    static void replacePicture() {

        System.out.print("Username: ");

        String username = scanner.nextLine().trim();


        int index = findUser(username);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        System.out.print("New picture file name: ");

        String picture = scanner.nextLine().trim();


        if (picture.isEmpty()) {

            System.out.println("Invalid picture name.");
            return;
        }


        pictureNames.set(index, picture);


        System.out.println("Picture replaced successfully.");
    }


    static void removePicture() {

        System.out.print("Username: ");

        String username = scanner.nextLine().trim();


        int index = findUser(username);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        pictureNames.set(index, "No picture");


        System.out.println("Picture removed successfully.");
    }


    static void searchUser() {

        System.out.print("Search username: ");

        String keyword = scanner.nextLine().toLowerCase().trim();


        boolean found = false;


        for (int i = 0; i < usernames.size(); i++) {


            if (usernames.get(i).toLowerCase().contains(keyword)) {


                System.out.println("----------------------");
                System.out.println("Username: " + usernames.get(i));
                System.out.println("Picture: " + pictureNames.get(i));


                found = true;
            }
        }


        if (!found) {

            System.out.println("No users found.");
        }
    }


    static void deleteUser() {

        System.out.print("Username: ");

        String username = scanner.nextLine().trim();


        int index = findUser(username);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        usernames.remove(index);
        pictureNames.remove(index);


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

    
}
