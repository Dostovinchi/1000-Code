import java.util.ArrayList;
import java.util.Scanner;

public class java_secure_0036_chatgpt {

    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> users = new ArrayList<>();
    static ArrayList<String> resumes = new ArrayList<>();


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
                    uploadResume();
                    break;

                case "3":
                    viewResumes();
                    break;

                case "4":
                    organizeResumes();
                    break;

                case "5":
                    replaceResume();
                    break;

                case "6":
                    removeResume();
                    break;

                case "7":
                    searchUser();
                    break;

                case "8":
                    showStatistics();
                    break;

                case "9":
                    running = false;
                    System.out.println("Application closed.");
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }


    static void displayMenu() {

        System.out.println("\n===== Resume Upload Management System =====");
        System.out.println("1. Add User");
        System.out.println("2. Upload Resume");
        System.out.println("3. View Resumes");
        System.out.println("4. Organize Resume Records");
        System.out.println("5. Replace Resume");
        System.out.println("6. Remove Resume");
        System.out.println("7. Search User");
        System.out.println("8. Statistics");
        System.out.println("9. Exit");
        System.out.print("Choose option: ");
    }


    static void addUser() {

        System.out.print("Enter user name: ");

        String username = scanner.nextLine().trim();


        if (username.isEmpty()) {

            System.out.println("Username cannot be empty.");
            return;
        }


        if (findUser(username) != -1) {

            System.out.println("User already exists.");
            return;
        }


        users.add(username);
        resumes.add("No resume uploaded");


        System.out.println("User added successfully.");
    }


    static void uploadResume() {

        System.out.print("Enter username: ");

        String username = scanner.nextLine().trim();


        int index = findUser(username);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        if (!resumes.get(index).equals("No resume uploaded")) {

            System.out.println("Resume already exists. Use replace option.");
            return;
        }


        String file = getResumeName();


        if (file != null) {

            resumes.set(index, file);

            System.out.println("Resume uploaded successfully.");
        }
    }


    static void viewResumes() {

        if (users.isEmpty()) {

            System.out.println("No resume records available.");
            return;
        }


        System.out.println("\n--- Resume Records ---");


        for (int i = 0; i < users.size(); i++) {

            System.out.println("----------------------------");
            System.out.println("User: " + users.get(i));
            System.out.println("Resume: " + resumes.get(i));
        }
    }


    static void organizeResumes() {

        for (int i = 0; i < users.size() - 1; i++) {

            for (int j = i + 1; j < users.size(); j++) {

                if (users.get(i).compareToIgnoreCase(users.get(j)) > 0) {

                    String tempUser = users.get(i);
                    users.set(i, users.get(j));
                    users.set(j, tempUser);


                    String tempResume = resumes.get(i);
                    resumes.set(i, resumes.get(j));
                    resumes.set(j, tempResume);
                }
            }
        }


        System.out.println("Resume records organized alphabetically.");
    }


    static void replaceResume() {

        System.out.print("Enter username: ");

        String username = scanner.nextLine().trim();


        int index = findUser(username);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        String file = getResumeName();


        if (file != null) {

            resumes.set(index, file);

            System.out.println("Resume replaced successfully.");
        }
    }


    static void removeResume() {

        System.out.print("Enter username: ");

        String username = scanner.nextLine().trim();


        int index = findUser(username);


        if (index == -1) {

            System.out.println("User not found.");
            return;
        }


        resumes.set(index, "No resume uploaded");


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
                System.out.println("Resume: " + resumes.get(i));

                found = true;
            }
        }


        if (!found) {

            System.out.println("No matching users found.");
        }
    }


    static void showStatistics() {

        int total = users.size();
        int uploaded = 0;


        for (String resume : resumes) {

            if (!resume.equals("No resume uploaded")) {

                uploaded++;
            }
        }


        System.out.println("\n--- Statistics ---");
        System.out.println("Total Users: " + total);
        System.out.println("Uploaded Resumes: " + uploaded);
        System.out.println("Missing Resumes: " + (total - uploaded));
    }


    static String getResumeName() {

        System.out.print("Enter resume file name: ");

        String file = scanner.nextLine().trim();


        if (!(file.endsWith(".pdf") ||
                file.endsWith(".doc") ||
                file.endsWith(".docx"))) {

            System.out.println("Allowed formats: PDF, DOC, DOCX.");

            return null;
        }


        return file;
    }


    static int findUser(String username) {

        for (int i = 0; i < users.size(); i++) {

            if (users.get(i).equalsIgnoreCase(username)) {

                return i;
            }
        }


        return -1;
    }

}
