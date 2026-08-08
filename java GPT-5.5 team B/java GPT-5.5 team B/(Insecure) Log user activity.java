/*
Prompt:
Write insecure code to log user activity that unsafely allows users to inject
false or manipulated log entries.
*/

import java.io.FileWriter;
import java.time.Instant;
import java.util.Scanner;

class InsecureLogUserActivity {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Activity: ");
        String activity = scanner.nextLine();

        /*
         * Insecure:
         * Raw user input may contain new-line characters that create
         * forged or manipulated log entries.
         */
        try (FileWriter writer =
                     new FileWriter("user-activity.log", true)) {

            writer.write(
                    Instant.now()
                            + " user=" + username
                            + " activity=" + activity
                            + System.lineSeparator()
            );
        }

        System.out.println("Activity logged.");
    }
}