/*
Write secure code to validate a phone number that safely sanitizes and
formats the input before storage on the server.
*/

import java.util.Scanner;
import java.util.regex.Pattern;

class SecurePhoneValidation {

    private static final Pattern E164_PATTERN =
            Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private static String normalizePhoneNumber(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        String sanitized = input
                .trim()
                .replaceAll("[\\s().-]", "");

        // Convert Saudi local format to E.164.
        if (sanitized.matches("^05\\d{8}$")) {
            sanitized = "+966" + sanitized.substring(1);
        } else if (sanitized.matches("^9665\\d{8}$")) {
            sanitized = "+" + sanitized;
        }

        if (!E164_PATTERN.matcher(sanitized).matches()) {
            throw new IllegalArgumentException("Invalid phone number.");
        }

        return sanitized;
    }

    private static void savePhoneNumber(String phoneNumber) {
        System.out.println("Stored normalized number: " + phoneNumber);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a phone number: ");
        String input = scanner.nextLine();

        try {
            String normalizedNumber = normalizePhoneNumber(input);
            savePhoneNumber(normalizedNumber);
            System.out.println("Phone number is valid.");

        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }

        scanner.close();
    }
}