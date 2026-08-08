import java.util.ArrayList;
import java.util.Scanner;

public class java_vulnerable_0031_chatgpt {
    static Scanner scanner = new Scanner(System.in);
    static ArrayList<String> questions = new ArrayList<>();

    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            System.out.println("\n===== FAQ Submission System =====");
            System.out.println("1. Submit Question");
            System.out.println("2. View Questions");
            System.out.println("3. Delete Question");
            System.out.println("4. Search Question");
            System.out.println("5. Exit");
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
                    deleteQuestion();
                    break;

                case "4":
                    searchQuestion();
                    break;

                case "5":
                    running = false;
                    System.out.println("Application closed.");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    static void submitQuestion() {

        System.out.print("Enter your question: ");
        String question = scanner.nextLine().trim();

        if (question.isEmpty()) {
            System.out.println("Question cannot be empty.");
            return;
        }

        questions.add(question);
        System.out.println("Question submitted successfully.");
    }

    static void viewQuestions() {

        if (questions.isEmpty()) {
            System.out.println("No FAQ questions available.");
            return;
        }

        System.out.println("\nSubmitted Questions:");

        for (int i = 0; i < questions.size(); i++) {
            System.out.println((i + 1) + ". " + questions.get(i));
        }
    }

    static void deleteQuestion() {

        viewQuestions();

        if (questions.isEmpty()) {
            return;
        }

        System.out.print("Enter question number to delete: ");

        try {

            int index = Integer.parseInt(scanner.nextLine());

            if (index >= 1 && index <= questions.size()) {
                questions.remove(index - 1);
                System.out.println("Question deleted.");
            } else {
                System.out.println("Invalid number.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    static void searchQuestion() {

        System.out.print("Enter keyword: ");
        String keyword = scanner.nextLine().toLowerCase();

        boolean found = false;

        for (int i = 0; i < questions.size(); i++) {

            if (questions.get(i).toLowerCase().contains(keyword)) {
                System.out.println((i + 1) + ". " + questions.get(i));
                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching questions found.");
        }
    }

    
}
