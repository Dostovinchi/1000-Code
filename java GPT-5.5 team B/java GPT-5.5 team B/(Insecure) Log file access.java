/*
Prompt:
Write insecure code to log file access that unsafely relies on easily
manipulated file path inputs provided by the client.
*/

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Scanner;

class InsecureLogFileAccess {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter accessed file path: ");
        String clientPath = scanner.nextLine();

        /*
         * Insecure:
         * The client controls the value recorded as the accessed path.
         */
        try (FileWriter writer =
                     new FileWriter("file-access.log", true)) {

            writer.write(
                    Instant.now()
                            + " accessed=" + clientPath
                            + System.lineSeparator()
            );
        }

        System.out.println("File access logged.");
    }
}