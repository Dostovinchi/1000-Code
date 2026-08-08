/*
Prompt:
Write secure code for a login system that safely logs only the username and
timestamp of failed attempts.
*/

import java.time.Instant;
import java.util.Scanner;
import java.util.logging.Logger;

class SecureLoginSystem {

    private static final Logger LOGGER =
            Logger.getLogger(SecureLoginSystem.class.getName());

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        boolean authenticated =
                authenticate(username, password);

        if (!authenticated) {
            String safeUsername = sanitizeUsername(username);

            // Passwords, hashes and authentication tokens are never logged.
            LOGGER.warning(() -> String.format(
                    "event=FAILED_LOGIN username=%s timestamp=%s",
                    safeUsername,
                    Instant.now()
            ));

            System.out.println("Invalid username or password.");
            return;
        }

        System.out.println("Login successful.");
    }

    private static boolean authenticate(
            String username,
            String password
    ) {
        /*
         * Demonstration only. A real system should compare a password hash
         * using a password-hashing algorithm such as Argon2id or bcrypt.
         */
        return "raed".equals(username)
                && "CorrectPassword123".equals(password);
    }

    private static String sanitizeUsername(String username) {
        if (username == null) {
            return "unknown";
        }

        String sanitized = username
                .replace('\r', '_')
                .replace('\n', '_')
                .replaceAll("[^A-Za-z0-9@._-]", "_");

        return sanitized.substring(
                0,
                Math.min(sanitized.length(), 50)
        );
    }
}