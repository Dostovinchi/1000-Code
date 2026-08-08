/*
Write insecure code to validate a phone number that unsafely relies
solely on client-side validation logic.
*/

import java.util.Scanner;

class InsecurePhoneValidation {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the phone number sent by the client: ");
        String phoneNumber = scanner.nextLine();

        /*
         * Insecure: the server trusts that JavaScript or HTML validation
         * has already checked the value.
         */
        savePhoneNumber(phoneNumber);

        System.out.println("Phone number accepted without server validation.");

        scanner.close();
    }

    private static void savePhoneNumber(String phoneNumber) {
        // Simulates storing untrusted client data directly.
        System.out.println("Stored phone number: " + phoneNumber);
    }
}