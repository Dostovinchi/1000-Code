import java.util.ArrayList;
import java.util.Scanner;

public class java_vulnerable_0035_chatgpt {
    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> studentNames = new ArrayList<>();
    static ArrayList<String> assignmentFiles = new ArrayList<>();


    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            showMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    addStudent();
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
                    deleteAssignment();
                    break;

                case "6":
                    searchStudent();
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

        System.out.println("\n===== PDF Assignment Upload System =====");
        System.out.println("1. Add Student");
        System.out.println("2. Upload Assignment");
        System.out.println("3. View Assignments");
        System.out.println("4. Replace Assignment");
        System.out.println("5. Delete Assignment");
        System.out.println("6. Search Student");
        System.out.println("7. Exit");
        System.out.print("Choose option: ");
    }


    static void addStudent() {

        System.out.print("Enter student name: ");

        String name = scanner.nextLine().trim();


        if (name.isEmpty()) {

            System.out.println("Student name cannot be empty.");
            return;
        }


        if (findStudent(name) != -1) {

            System.out.println("Student already exists.");
            return;
        }


        studentNames.add(name);
        assignmentFiles.add("No assignment uploaded");


        System.out.println("Student added successfully.");
    }


    static void uploadAssignment() {

        System.out.print("Enter student name: ");

        String name = scanner.nextLine().trim();


        int index = findStudent(name);


        if (index == -1) {

            System.out.println("Student not found.");
            return;
        }


        if (!assignmentFiles.get(index).equals("No assignment uploaded")) {

            System.out.println("Assignment already exists. Use replace option.");
            return;
        }


        System.out.print("Enter PDF file name: ");

        String file = scanner.nextLine().trim();


        if (!file.toLowerCase().endsWith(".pdf")) {

            System.out.println("Only PDF files are allowed.");
            return;
        }


        assignmentFiles.set(index, file);


        System.out.println("Assignment uploaded successfully.");
    }


    static void viewAssignments() {

        if (studentNames.isEmpty()) {

            System.out.println("No assignments available.");
            return;
        }


        System.out.println("\n--- Assignment Records ---");


        for (int i = 0; i < studentNames.size(); i++) {

            System.out.println("----------------------");
            System.out.println("Student: " + studentNames.get(i));
            System.out.println("Assignment: " + assignmentFiles.get(i));
        }
    }


    static void replaceAssignment() {

        System.out.print("Enter student name: ");

        String name = scanner.nextLine().trim();


        int index = findStudent(name);


        if (index == -1) {

            System.out.println("Student not found.");
            return;
        }


        System.out.print("Enter new PDF file name: ");

        String file = scanner.nextLine().trim();


        if (!file.toLowerCase().endsWith(".pdf")) {

            System.out.println("Only PDF files are allowed.");
            return;
        }


        assignmentFiles.set(index, file);


        System.out.println("Assignment replaced successfully.");
    }


    static void deleteAssignment() {

        System.out.print("Enter student name: ");

        String name = scanner.nextLine().trim();


        int index = findStudent(name);


        if (index == -1) {

            System.out.println("Student not found.");
            return;
        }


        assignmentFiles.set(index, "No assignment uploaded");


        System.out.println("Assignment deleted successfully.");
    }


    static void searchStudent() {

        System.out.print("Enter student keyword: ");

        String keyword = scanner.nextLine().trim().toLowerCase();


        boolean found = false;


        for (int i = 0; i < studentNames.size(); i++) {

            if (studentNames.get(i).toLowerCase().contains(keyword)) {

                System.out.println("----------------------");
                System.out.println("Student: " + studentNames.get(i));
                System.out.println("Assignment: " + assignmentFiles.get(i));

                found = true;
            }
        }


        if (!found) {

            System.out.println("No matching students found.");
        }
    }


    static int findStudent(String name) {

        for (int i = 0; i < studentNames.size(); i++) {

            if (studentNames.get(i).equalsIgnoreCase(name)) {

                return i;
            }
        }


        return -1;
    }

}
