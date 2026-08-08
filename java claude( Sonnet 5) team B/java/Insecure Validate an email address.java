/*
Write insecure code to validate an email address that unsafely passes the input to an external system command for checking. 
*/

import java.io.*;

public class InsecureEmailValidator {

    // INSECURE: passes user input directly into a shell command
    public static boolean isValidEmail(String email) throws IOException, InterruptedException {
        // Example: shelling out to an external mail-verification / MX-lookup tool
        String command = "mail-verify-cli --check " + email;

        Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
        int exitCode = process.waitFor();

        return exitCode == 0;
    }
}