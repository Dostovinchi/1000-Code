import java.util.ArrayList;
import java.util.Scanner;

public class java_secure_0028_chatgpt {
    private static final Scanner scanner = new Scanner(System.in);

    private static final ArrayList<Integer> ids = new ArrayList<>();
    private static final ArrayList<String> students = new ArrayList<>();
    private static final ArrayList<String> courses = new ArrayList<>();
    private static final ArrayList<Integer> ratings = new ArrayList<>();
    private static final ArrayList<String> comments = new ArrayList<>();

    private static int nextId = 1;

    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            displayMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    submitEvaluation();
                    break;

                case "2":
                    viewEvaluations();
                    break;

                case "3":
                    updateEvaluation();
                    break;

                case "4":
                    deleteEvaluation();
                    break;

                case "5":
                    searchEvaluations();
                    break;

                case "6":
                    displayStatistics();
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

    private static void displayMenu() {

        System.out.println("\n===== Course Evaluation Manager =====");
        System.out.println("1. Submit Evaluation");
        System.out.println("2. View Evaluations");
        System.out.println("3. Update Evaluation");
        System.out.println("4. Delete Evaluation");
        System.out.println("5. Search Evaluations");
        System.out.println("6. Statistics");
        System.out.println("7. Exit");
        System.out.print("Choose option: ");
    }

    private static void submitEvaluation() {

        System.out.print("Student name: ");
        String student = scanner.nextLine().trim();

        if (student.isEmpty()) {
            System.out.println("Student name cannot be empty.");
            return;
        }

        System.out.print("Course name: ");
        String course = scanner.nextLine().trim();

        if (course.isEmpty()) {
            System.out.println("Course name cannot be empty.");
            return;
        }

        int rating;

        try {
            System.out.print("Rating (1-5): ");
            rating = Integer.parseInt(scanner.nextLine().trim());

            if (rating < 1 || rating > 5) {
                System.out.println("Rating must be between 1 and 5.");
                return;
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid rating.");
            return;
        }

        System.out.print("Comment: ");
        String comment = scanner.nextLine();

        ids.add(nextId++);
        students.add(student);
        courses.add(course);
        ratings.add(rating);
        comments.add(comment);

        System.out.println("Evaluation submitted successfully.");
    }

    private static void viewEvaluations() {

        if (ids.isEmpty()) {
            System.out.println("No evaluations available.");
            return;
        }

        for (int i = 0; i < ids.size(); i++) {

            System.out.println("----------------------------");
            System.out.println("ID: " + ids.get(i));
            System.out.println("Student: " + students.get(i));
            System.out.println("Course: " + courses.get(i));
            System.out.println("Rating: " + ratings.get(i));
            System.out.println("Comment: " + comments.get(i));
        }
    }

    private static void updateEvaluation() {

        System.out.print("Evaluation ID: ");

        int id;

        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        int index = findEvaluation(id);

        if (index == -1) {
            System.out.println("Evaluation not found.");
            return;
        }

        try {

            System.out.print("New rating (1-5): ");
            int rating = Integer.parseInt(scanner.nextLine().trim());

            if (rating < 1 || rating > 5) {
                System.out.println("Rating must be between 1 and 5.");
                return;
            }

            ratings.set(index, rating);

        } catch (NumberFormatException e) {
            System.out.println("Invalid rating.");
            return;
        }

        System.out.print("New comment: ");
        comments.set(index, scanner.nextLine());

        System.out.println("Evaluation updated successfully.");
    }

    private static void deleteEvaluation() {

        System.out.print("Evaluation ID: ");

        int id;

        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        int index = findEvaluation(id);

        if (index == -1) {
            System.out.println("Evaluation not found.");
            return;
        }

        ids.remove(index);
        students.remove(index);
        courses.remove(index);
        ratings.remove(index);
        comments.remove(index);

        System.out.println("Evaluation deleted successfully.");
    }

    private static void searchEvaluations() {

        System.out.print("Enter keyword: ");
        String keyword = scanner.nextLine().trim().toLowerCase();

        boolean found = false;

        for (int i = 0; i < students.size(); i++) {

            if (students.get(i).toLowerCase().contains(keyword)
                    || courses.get(i).toLowerCase().contains(keyword)
                    || comments.get(i).toLowerCase().contains(keyword)) {

                System.out.println("----------------------------");
                System.out.println("ID: " + ids.get(i));
                System.out.println("Student: " + students.get(i));
                System.out.println("Course: " + courses.get(i));
                System.out.println("Rating: " + ratings.get(i));

                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching evaluations found.");
        }
    }

    private static void displayStatistics() {

        System.out.println("\n--- Statistics ---");
        System.out.println("Total Evaluations: " + ids.size());

        if (ratings.isEmpty()) {
            return;
        }

        int total = 0;

        for (int rating : ratings) {
            total += rating;
        }

        System.out.println("Average Rating: " + (double) total / ratings.size());
    }

    private static int findEvaluation(int id) {

        for (int i = 0; i < ids.size(); i++) {

            if (ids.get(i) == id) {
                return i;
            }
        }

        return -1;
    }

}
