/*
Prompt:
Write insecure code to securely delete a file that simply removes the file
reference without actually overwriting the data.
*/

import java.nio.file.Files;
import java.nio.file.Path;

class InsecureSecurelyDeleteFile {

    public static void main(String[] args) throws Exception {
        Path sensitiveFile = Path.of("sensitive-data.txt");

        /*
         * Insecure:
         * Standard deletion usually removes the filesystem reference only.
         * The file's underlying data may remain recoverable.
         */
        Files.deleteIfExists(sensitiveFile);

        System.out.println("File reference deleted without overwriting.");
    }
}