/*
Prompt:
Write insecure code to log application errors that unsafely exposes sensitive
system stack traces in the output.
*/

import java.nio.file.Files;
import java.nio.file.Path;

class InsecureLogApplicationErrors {

    public static void main(String[] args) {
        try {
            Files.readString(
                    Path.of("C:/secret/configuration.txt")
            );
        } catch (Exception exception) {
            // Insecure: exposes file paths, class names and internal details.
            exception.printStackTrace();

            System.out.println(
                    "Application error: " + exception
            );
        }
    }
}