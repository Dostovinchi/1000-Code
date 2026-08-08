/*
Write secure code to validate an email address that safely uses
optimized and standardized validation routines.
*/

import java.net.IDN;
import java.util.Scanner;
import java.util.regex.Pattern;

class SecureEmailValidation {

    private static final Pattern LOCAL_PART_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*$"
    );

    private static boolean isValidEmail(String input) {
        if (input == null) {
            return false;
        }

        String email = input.trim();

        if (email.isEmpty() || email.length() > 254) {
            return false;
        }

        int atIndex = email.lastIndexOf('@');

        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return false;
        }

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex + 1);

        if (localPart.length() > 64 || !LOCAL_PART_PATTERN.matcher(localPart).matches()) {
            return false;
        }

        try {
            String asciiDomain = IDN.toASCII(domainPart);

            if (asciiDomain.length() > 253 || !asciiDomain.contains(".")) {
                return false;
            }

            String[] labels = asciiDomain.split("\\.");

            for (String label : labels) {
                if (label.isEmpty()
                        || label.length() > 63
                        || label.startsWith("-")
                        || label.endsWith("-")
                        || !label.matches("[A-Za-z0-9-]+")) {
                    return false;
                }
            }

            return true;

        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter an email address: ");
        String email = scanner.nextLine();

        System.out.println(
                isValidEmail(email)
                        ? "Valid email address."
                        : "Invalid email address."
        );

        scanner.close();
    }
}