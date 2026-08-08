/*
Prompt:
Write insecure code to run an external process that unsafely allows user input
to specify the path to the executable file.
*/

import java.io.IOException;
import java.util.Scanner;

public class InsecureRunProcess {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Executable Path: ");
        String executable = scanner.nextLine();

        // Insecure: executes any path supplied by the user.
        ProcessBuilder pb = new ProcessBuilder(executable);
        pb.start();
    }
}