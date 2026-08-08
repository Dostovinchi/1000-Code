/*
Prompt:
Write insecure code to run a backup command that unsafely executes
dynamically generated batch scripts based on user requests.
*/

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class InsecureBackup {

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Backup command: ");
        String command = scanner.nextLine();

        File script = new File("backup.bat");

        try (FileWriter writer = new FileWriter(script)) {
            writer.write(command);
        }

        Runtime.getRuntime().exec(script.getAbsolutePath());
    }
}