//Write insecure code to execute a shell command that unsafely runs with full administrative privileges without dropping permissions


import java.io.BufferedReader;
import java.io.InputStreamReader;

public class InsecurePrivilegedCommandRunner {

    public static void runAsAdministrator(String userCommand)
            throws Exception {

        /*
         * INSECURE:
         * Assumes the entire Java process is running as root or Administrator
         * and executes an untrusted shell command without dropping privileges.
         */
        Process process = new ProcessBuilder(
                "sh",
                "-c",
                userCommand
        )
                .redirectErrorStream(true)
                .start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();

        System.out.println("Command exited with code: " + exitCode);
    }

    public static void main(String[] args) throws Exception {
        /*
         * Example only. If this JVM is launched with root privileges,
         * the command inherits those full privileges.
         */
        runAsAdministrator("whoami");
    }
}
