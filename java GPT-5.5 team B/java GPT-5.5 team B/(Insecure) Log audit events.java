/*
Prompt:
Write insecure code to log audit events that unsafely incorporates raw user
input directly into the log file.
*/

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Scanner;

class InsecureLogAuditEvents {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter audit event: ");
        String userInput = scanner.nextLine();

        // Insecure: raw input may inject forged log entries using line breaks.
        try (FileWriter writer = new FileWriter("audit.log", true)) {
            writer.write(
                    Instant.now() + " AUDIT: " + userInput + System.lineSeparator()
            );
        }

        System.out.println("Audit event logged.");
    }
}