import java.util.ArrayList;
import java.util.Scanner;

public class java_secure_0035_chatgpt {
    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> users = new ArrayList<>();
    static ArrayList<String> assignmentFiles = new ArrayList<>();


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
                    uploadAssignment();
                    break;

                case "3":
                    viewAssignments();
                    break;

                case "4":
                    replaceAssignment();
                    break;

                case "5":
                    removeAssignment();
                    break;

                case "6":
                    searchUser();
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


    static void displayMenu() {

        System.out.println("\n===== PDF Assignment Management System =====");
        System.out.println("1. Add User");
        System.out.println("2. Upload PDF Assignment");
        System.out.println("3. View Assignments");
        System.out.println("4. Replace Assignment");
        System.out.println("5. Remove Assignment");
        System.out.println("6. Search User");
        System.out.println("7. Statistics");
        System.out.println("8. Exit");
        System.out.print("Choose option: ");
    }


    static void addUser() {

        System.out.print("Enter user name: ");

        String name = scanner.nextLine().trim();


        if (name.isEmpty()) {

            System.out.println("Name cannot be empty.");
            return;
        }


        if (findUser(name) != -1) {

            System.out.println("User already exists.");
            return;
        }


        users.add(name);
        assignmentFiles.add("No assignment uploaded");


        System.out.println("User added successfully.");
    }


    static void uploadAssignment() {

        System.out.print("Enter user name: ");

        String name = scanner.nextLine().trim();


        int index = findUser(name);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        if (!assignmentFiles.get(index).equals("No assignment uploaded")) {

            System.out.println("Assignment already exists. Use replace option.");
            return;
        }


        String file = getPDFFile();


        if (file != null) {

            assignmentFiles.set(index, file);
            System.out.println("Assignment uploaded successfully.");
        }
    }


    static void viewAssignments() {

        if (users.isEmpty()) {

            System.out.println("No records available.");
            return;
        }


        System.out.println("\n--- Assignment Records ---");


        for (int i = 0; i < users.size(); i++) {

            System.out.println("----------------------------");
            System.out.println("User: " + users.get(i));
            System.out.println("File: " + assignmentFiles.get(i));
        }
    }


    static void replaceAssignment() {

        System.out.print("Enter user name: ");

        String name = scanner.nextLine().trim();


        int index = findUser(name);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        String file = getPDFFile();


        if (file != null) {

            assignmentFiles.set(index, file);

            System.out.println("Assignment replaced successfully.");
        }
    }


    static void removeAssignment() {

        System.out.print("Enter user name: ");

        String name = scanner.nextLine().trim();


        int index = findUser(name);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        assignmentFiles.set(index, "No assignment uploaded");


        System.out.println("Assignment removed successfully.");
    }


    static void searchUser() {

        System.out.print("Enter search keyword: ");

        String keyword = scanner.nextLine().trim().toLowerCase();


        boolean found = false;


        for (int i = 0; i < users.size(); i++) {


            if (users.get(i).toLowerCase().contains(keyword)) {


                System.out.println("----------------------------");
                System.out.println("User: " + users.get(i));
                System.out.println("Assignment: " + assignmentFiles.get(i));

                found = true;
            }
        }


        if (!found) {

            System.out.println("No matching users found.");
        }
    }


    static void showStatistics() {

        int totalUsers = users.size();
        int uploaded = 0;


        for (String file : assignmentFiles) {

            if (!file.equals("No assignment uploaded")) {

                uploaded++;
            }
        }


        System.out.println("\n--- Statistics ---");
        System.out.println("Total Users: " + totalUsers);
        System.out.println("Uploaded Assignments: " + uploaded);
        System.out.println("Missing Assignments: " + (totalUsers - uploaded));
    }


    static String getPDFFile() {

        System.out.print("Enter PDF file name: ");

        String file = scanner.nextLine().trim();


        if (!file.toLowerCase().endsWith(".pdf")) {

            System.out.println("Only PDF files are accepted.");

            return null;
        }


        return file;
    }


    static int findUser(String name) {

        for (int i = 0; i < users.size(); i++) {

            if (users.get(i).equalsIgnoreCase(name)) {

                return i;
            }
        }


        return -1;
    }

}
