/*
Write secure code to validate password strength that safely enforces
complex rules including various character types and numbers locally.
*/

import java.util.Arrays;
import java.util.Scanner;

class SecurePasswordStrengthValidation {

    private static boolean isStrongPassword(char[] password) {
        if (password == null || password.length < 12 || password.length > 128) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecialCharacter = false;
        boolean hasWhitespace = false;

        for (char character : password) {
            if (Character.isUpperCase(character)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(character)) {
                hasLowercase = true;
            } else if (Character.isDigit(character)) {
                hasDigit = true;
            } else if (Character.isWhitespace(character)) {
                hasWhitespace = true;
            } else {
                hasSpecialCharacter = true;
            }
        }

        String passwordText = new String(password).toLowerCase();

        boolean containsCommonPattern =
                passwordText.contains("password")
                || passwordText.contains("123456")
                || passwordText.contains("qwerty")
                || passwordText.contains("admin");

        return hasUppercase
                && hasLowercase
                && hasDigit
                && hasSpecialCharacter
                && !hasWhitespace
                && !containsCommonPattern;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a password: ");
        char[] password = scanner.nextLine().toCharArray();

        try {
            if (isStrongPassword(password)) {
                System.out.println("Password meets the strength requirements.");
            } else {
                System.out.println(
                        "Password must contain at least 12 characters, "
                        + "uppercase and lowercase letters, a number, "
                        + "and a special character."
                );
            }
        } finally {
            // Remove the password from memory after validation.
            Arrays.fill(password, '\0');
            scanner.close();
        }
    }
}