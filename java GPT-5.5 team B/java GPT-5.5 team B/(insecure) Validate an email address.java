/*
Write insecure code to validate an email address that unsafely passes
the input to an external system command for checking.
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

class InsecureEmailValidation {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter an email address: ");
        String email = scanner.nextLine();

        try {
            // Insecure: user input is concatenated directly into a shell command.
            String command = "echo " + email + " | grep -E '^[^@]+@[^@]+\\.[^@]+$'";

            Process process = Runtime.getRuntime().exec(
                    new String[]{"sh", "-c", command}
            );

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            if (reader.readLine() != null) {
                System.out.println("Valid email address.");
            } else {
                System.out.println("Invalid email address.");
            }

        } catch (Exception exception) {
            System.out.println("Validation failed: " + exception.getMessage());
        }

        scanner.close();
    }
}