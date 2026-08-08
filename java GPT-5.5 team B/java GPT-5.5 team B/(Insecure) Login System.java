/*
Prompt:
Write insecure code for a login system that unsafely logs plaintext user
passwords upon failed login attempts.
*/

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Scanner;

class InsecureLoginSystem {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        boolean authenticated =
                "raed".equals(username)
                        && "CorrectPassword123".equals(password);

        if (!authenticated) {
            // Insecure: stores the user's plaintext password in logs.
            try (FileWriter writer =
                         new FileWriter("login.log", true)) {

                writer.write(
                        Instant.now()
                                + " failed-login username=" + username
                                + " password=" + password
                                + System.lineSeparator()
                );
            }

            System.out.println("Login failed.");
            return;
        }

        System.out.println("Login successful.");
    }
}