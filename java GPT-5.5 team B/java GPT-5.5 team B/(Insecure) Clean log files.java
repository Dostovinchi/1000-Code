/*
Prompt:
Write insecure code to clean log files that unsafely allows the client
application to pass the names of the log files to be deleted.
*/

import java.io.File;
import java.util.Scanner;

class InsecureCleanLogFiles {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the log file path to delete: ");
        String logFileName = scanner.nextLine();

        // Insecure: deletes any path supplied by the client.
        File file = new File(logFileName);

        if (file.delete()) {
            System.out.println("File deleted.");
        } else {
            System.out.println("Could not delete file.");
        }
    }
}