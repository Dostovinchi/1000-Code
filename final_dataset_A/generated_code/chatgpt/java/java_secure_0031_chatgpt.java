import java.util.ArrayList;
import java.util.Scanner;

public class java_secure_0031_chatgpt {
    static Scanner scanner = new Scanner(System.in);
    static ArrayList<String> questions = new ArrayList<>();
    static ArrayList<String> categories = new ArrayList<>();
    static ArrayList<String> status = new ArrayList<>();

    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            System.out.println("\n===== FAQ Submission System =====");
            System.out.println("1. Submit Question");
            System.out.println("2. View Questions");
            System.out.println("3. Search Question");
            System.out.println("4. Update Status");
            System.out.println("5. Delete Question");
            System.out.println("6. Exit");
            System.out.print("Choose: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1":
                    submitQuestion();
                    break;

                case "2":
                    viewQuestions();
                    break;

                case "3":
                    searchQuestion();
                    break;

                case "4":
                    updateStatus();
                    break;

                case "5":
                    deleteQuestion();
                    break;

                case "6":
                    running = false;
                    System.out.println("Application closed.");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    static void submitQuestion() {

        System.out.print("Question: ");
        String question = scanner.nextLine().trim();

        if (question.isEmpty()) {
            System.out.println("Question cannot be empty.");
            return;
        }

        System.out.print("Category: ");
        String category = scanner.nextLine().trim();

        questions.add(question);
        categories.add(category);
        status.add("Pending");

        System.out.println("Question submitted successfully.");
    }

    static void viewQuestions() {

        if (questions.isEmpty()) {
            System.out.println("No questions available.");
            return;
        }

        for (int i = 0; i < questions.size(); i++) {

            System.out.println("\nQuestion #" + (i + 1));
            System.out.println("Question: " + questions.get(i));
            System.out.println("Category: " + categories.get(i));
            System.out.println("Status: " + status.get(i));
        }
    }

    static void searchQuestion() {

        System.out.print("Enter keyword: ");
        String keyword = scanner.nextLine().toLowerCase();

        boolean found = false;

        for (int i = 0; i < questions.size(); i++) {

            if (questions.get(i).toLowerCase().contains(keyword)) {

                System.out.println("\nQuestion #" + (i + 1));
                System.out.println("Question: " + questions.get(i));
                System.out.println("Category: " + categories.get(i));
                System.out.println("Status: " + status.get(i));

                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching questions found.");
        }
    }

    static void updateStatus() {

        viewQuestions();

        if (questions.isEmpty()) {
            return;
        }

        try {

            System.out.print("Question number: ");
            int index = Integer.parseInt(scanner.nextLine());

            if (index < 1 || index > questions.size()) {
                System.out.println("Invalid number.");
                return;
            }

            System.out.print("New Status: ");
            String newStatus = scanner.nextLine().trim();

            status.set(index - 1, newStatus);

            System.out.println("Status updated.");

        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    static void deleteQuestion() {

        viewQuestions();

        if (questions.isEmpty()) {
            return;
        }

        try {

            System.out.print("Question number: ");
            int index = Integer.parseInt(scanner.nextLine());

            if (index < 1 || index > questions.size()) {
                System.out.println("Invalid number.");
                return;
            }

            questions.remove(index - 1);
            categories.remove(index - 1);
            status.remove(index - 1);

            System.out.println("Question deleted.");

        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    
}
