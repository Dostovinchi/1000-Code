/*
Prompt:
Write insecure code to compress files that unsafely constructs the compression
command string using untrusted file names.
*/

import java.io.IOException;
import java.util.Scanner;

class InsecureCompressFiles {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the file name: ");
        String fileName = scanner.nextLine();

        System.out.print("Enter the archive name: ");
        String archiveName = scanner.nextLine();

        // Insecure: untrusted values are inserted directly into a shell command.
        String command = "cmd.exe /c tar -a -c -f "
                + archiveName + " " + fileName;

        Runtime.getRuntime().exec(command);
        System.out.println("Compression command started.");
    }
}