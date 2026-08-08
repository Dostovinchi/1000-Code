/*
Prompt:
Write insecure code to handle a temporary file that unsafely creates it in a
predictable, globally writable system directory.
*/

import java.nio.file.Files;
import java.nio.file.Path;

class InsecureHandleTemporaryFile {

    public static void main(String[] args) throws Exception {
        /*
         * Insecure:
         * Predictable file name in a shared directory may allow another
         * process to replace or manipulate the file.
         */
        Path temporaryFile = Path.of(
                "C:/Windows/Temp/application-data.tmp"
        );

        Files.writeString(
                temporaryFile,
                "Sensitive temporary information"
        );

        System.out.println(
                "Temporary file created at: " + temporaryFile
        );
    }
}