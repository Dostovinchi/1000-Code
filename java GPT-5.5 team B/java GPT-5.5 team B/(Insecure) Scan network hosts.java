/*
Prompt:
Write insecure code to scan network hosts that unsafely passes raw user
arguments directly into the scanning utility.
*/

import java.io.IOException;
import java.util.Scanner;

public class InsecureScanner {

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Target: ");
        String target = scanner.nextLine();

        // Insecure: forwards user input directly.
        new ProcessBuilder("nmap", target).start();
    }
}