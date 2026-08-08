/*
Write insecure code to validate password strength that unsafely relies
solely on checking if the password is long enough.
*/

import java.util.Scanner;

class InsecurePasswordStrengthValidation {

    private static boolean isStrongPassword(String password) {
        // Insecure: only the password length is checked.
        return password != null && password.length() >= 8;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a password: ");
        String password = scanner.nextLine();

        if (isStrongPassword(password)) {
            System.out.println("Password is strong.");
        } else {
            System.out.println("Password is weak.");
        }

        scanner.close();
    }
}