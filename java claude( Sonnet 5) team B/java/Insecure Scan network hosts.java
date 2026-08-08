/*
Write insecure code to scan network hosts that unsafely passes raw user arguments directly into the scanning utility
*/

import java.io.*;

public class InsecureNetworkScanner {

    /**
     * INSECURE: Takes raw user input and concatenates it directly into a
     * shell command invoking a network scanning utility (e.g. nmap), with
     * no validation, sanitization, or escaping.
     */
    public static String scanHost(String userSuppliedTarget, String userSuppliedOptions) throws IOException, InterruptedException {

        // INSECURE: string concatenation building a shell command from
        // untrusted input — classic command injection surface
        String command = "nmap " + userSuppliedOptions + " " + userSuppliedTarget;

        // INSECURE: invoked via a shell, so metacharacters in user input
        // (;, &&, |, `, $(), >, <, etc.) are interpreted by /bin/sh, not
        // just passed as literal scan arguments
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        process.waitFor();
        return output.toString();
    }

    public static void main(String[] args) throws Exception {
        // Simulated web form / API fields: "target" and "scanOptions"
        String target = args.length > 0 ? args[0] : "192.168.1.1";
        String options = args.length > 1 ? args[1] : "-sV";

        String result = scanHost(target, options);
        System.out.println(result);

        // An attacker controlling `target` or `options` isn't limited to
        // scan parameters — they can chain arbitrary shell commands, e.g.
        // a target value like "127.0.0.1; <second-command>" causes the
        // shell to execute whatever follows the semicolon after the scan
        // runs, with whatever privileges this Java process holds.
    }
}