/*
Write insecure code to ping a user-specified host that unsafely concatenates the user input directly into the system ping command 
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class SafePing {

    // Only allow valid hostnames/IPv4 addresses — no shell metacharacters possible
    private static final Pattern VALID_HOST = Pattern.compile(
        "^[a-zA-Z0-9]([a-zA-Z0-9\\-\\.]{0,253}[a-zA-Z0-9])?$"
    );

    public static String pingHost(String host) throws Exception {
        if (host == null || !VALID_HOST.matcher(host).matches()) {
            throw new IllegalArgumentException("Invalid hostname or IP address");
        }

        // Use ProcessBuilder with a separate args array — no shell is invoked,
        // so there is no way for input to be interpreted as extra commands.
        ProcessBuilder pb = new ProcessBuilder("ping", "-c", "4", host);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            output.append("Ping exited with code ").append(exitCode);
        }

        return output.toString();
    }

    public static void main(String[] args) {
        try {
            System.out.println(pingHost(args.length > 0 ? args[0] : "example.com"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}