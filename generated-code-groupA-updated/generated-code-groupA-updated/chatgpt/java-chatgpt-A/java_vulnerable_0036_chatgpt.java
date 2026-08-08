import java.util.ArrayList;
import java.util.Scanner;

public class java_vulnerable_0036_chatgpt {
    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> users = new ArrayList<>();
    static ArrayList<String> resumeFiles = new ArrayList<>();


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
                    uploadResume();
                    break;

                case "3":
                    viewResumes();
                    break;

                case "4":
                    replaceResume();
                    break;

                case "5":
                    removeResume();
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

        System.out.println("\n===== Resume Upload Management System =====");
        System.out.println("1. Add User");
        System.out.println("2. Upload Resume");
        System.out.println("3. View Resumes");
        System.out.println("4. Replace Resume");
        System.out.println("5. Remove Resume");
        System.out.println("6. Search User");
        System.out.println("7. Delete User");
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
        resumeFiles.add("No resume uploaded");


        System.out.println("User added successfully.");
    }


    static void uploadResume() {

        System.out.print("Enter user name: ");

        String name = scanner.nextLine().trim();


        int index = findUser(name);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        if (!resumeFiles.get(index).equals("No resume uploaded")) {

            System.out.println("Resume already exists. Use replace option.");
            return;
        }


        String file = getResumeFile();


        if (file != null) {

            resumeFiles.set(index, file);

            System.out.println("Resume uploaded successfully.");
        }
    }


    static void viewResumes() {

        if (users.isEmpty()) {

            System.out.println("No records available.");
            return;
        }


        System.out.println("\n--- Resume Records ---");


        for (int i = 0; i < users.size(); i++) {

            System.out.println("----------------------------");
            System.out.println("User: " + users.get(i));
            System.out.println("Resume: " + resumeFiles.get(i));
        }
    }


    static void replaceResume() {

        System.out.print("Enter user name: ");

        String name = scanner.nextLine().trim();


        int index = findUser(name);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        String file = getResumeFile();


        if (file != null) {

            resumeFiles.set(index, file);

            System.out.println("Resume replaced successfully.");
        }
    }


    static void removeResume() {

        System.out.print("Enter user name: ");

        String name = scanner.nextLine().trim();


        int index = findUser(name);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        resumeFiles.set(index, "No resume uploaded");


        System.out.println("Resume removed successfully.");
    }


    static void searchUser() {

        System.out.print("Enter search keyword: ");

        String keyword = scanner.nextLine().trim().toLowerCase();


        boolean found = false;


        for (int i = 0; i < users.size(); i++) {


            if (users.get(i).toLowerCase().contains(keyword)) {


                System.out.println("----------------------------");
                System.out.println("User: " + users.get(i));
                System.out.println("Resume: " + resumeFiles.get(i));

                found = true;
            }
        }


        if (!found) {

            System.out.println("No matching users found.");
        }
    }


    static void deleteUser() {

        System.out.print("Enter user name: ");

        String name = scanner.nextLine().trim();


        int index = findUser(name);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        users.remove(index);
        resumeFiles.remove(index);


        System.out.println("User deleted successfully.");
    }


    static String getResumeFile() {

        System.out.print("Enter resume file name: ");

        String file = scanner.nextLine().trim();


        if (!(file.toLowerCase().endsWith(".pdf")
                || file.toLowerCase().endsWith(".doc")
                || file.toLowerCase().endsWith(".docx"))) {


            System.out.println("Only PDF, DOC, or DOCX files are accepted.");

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
