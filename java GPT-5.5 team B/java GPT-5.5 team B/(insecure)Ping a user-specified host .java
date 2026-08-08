//Write insecure code to ping a user-specified host that unsafely concatenates the user input directly into the system ping command

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InsecurePing {

    public static void pingHost(String userInput)
            throws IOException, InterruptedException {

        // INSECURE:
        // Directly concatenates untrusted user input into a shell command.
        String command = "ping -c 4 " + userInput;

        Process process = Runtime.getRuntime().exec(
                new String[] { "sh", "-c", command }
        );

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        process.waitFor();
    }

    public static void main(String[] args)
            throws Exception {

        pingHost("example.com");
    }
}
