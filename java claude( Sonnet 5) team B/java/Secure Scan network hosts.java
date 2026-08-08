/*
 Write secure code to scan network hosts that safely uses pre-approved, safe configuration flags and dedicated networking APIs 
*/

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SecureNetworkScanner {

    private static final Logger logger = Logger.getLogger(SecureNetworkScanner.class.getName());

    // Fixed path to the trusted scanning binary — never derived from user input
    private static final String NMAP_PATH = "/usr/bin/nmap";

    // Strict allowlist of permitted scan option flags. No script execution
    // flags (--script), no output-to-file flags, nothing that reads/writes
    // arbitrary paths or executes external code.
    private static final Set<String> ALLOWED_FLAGS = Set.of(
            "-sV",   // version detection
            "-sT",   // TCP connect scan
            "-Pn",   // skip host discovery
            "-F"     // fast scan (common ports only)
    );

    // Only allow scanning targets within an explicitly approved internal range
    private static final Pattern PRIVATE_IPV4 = Pattern.compile(
            "^(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|" +
            "192\\.168\\.\\d{1,3}\\.\\d{1,3}|" +
            "172\\.(1[6-9]|2\\d|3[0-1])\\.\\d{1,3}\\.\\d{1,3})$");

    private static final int PROCESS_TIMEOUT_SECONDS = 60;

    /**
     * Validates that the target is a syntactically well-formed IPv4 address
     * within an approved private range. Adjust the allowed ranges to match
     * your actual authorized scanning scope.
     */
    private static void validateTarget(String target) {
        if (target == null || !PRIVATE_IPV4.matcher(target).matches()) {
            throw new IllegalArgumentException("Target is not an approved scan destination");
        }
    }

    /**
     * Validates requested flags against the fixed allowlist. Any flag not
     * explicitly recognized is rejected outright rather than passed through.
     */
    private static List<String> validateFlags(List<String> requestedFlags) {
        List<String> validated = new ArrayList<>();
        for (String flag : requestedFlags) {
            if (!ALLOWED_FLAGS.contains(flag)) {
                throw new IllegalArgumentException("Flag not permitted: " + flag);
            }
            validated.add(flag);
        }
        return validated;
    }

    /**
     * Runs nmap with validated target and allowlisted flags, using an
     * argument array (no shell interpretation) so metacharacters in any
     * input are treated as literal data, not shell syntax.
     */
    public static String scanHost(String target, List<String> requestedFlags)
            throws IOException, InterruptedException {

        validateTarget(target);
        List<String> flags = validateFlags(requestedFlags);

        List<String> commandParts = new ArrayList<>();
        commandParts.add(NMAP_PATH);
        commandParts.addAll(flags);
        commandParts.add(target);

        // No "/bin/sh -c" — arguments passed directly as argv, so there is
        // no shell to interpret ;, &&, |, `, $(), etc.
        ProcessBuilder pb = new ProcessBuilder(commandParts);
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

        boolean finished = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            logger.warning("Scan of " + target + " timed out and was terminated");
            throw new IOException("Scan timed out");
        }

        logger.info("Scan completed for target=" + target + " flags=" + flags);
        return output.toString();
    }

    /**
     * Lightweight alternative for simple reachability/port-open checks that
     * avoids shelling out entirely, using only Java's built-in networking
     * APIs. Preferable when full nmap-style scanning isn't actually needed.
     */
    public static boolean isPortOpen(String target, int port, int timeoutMs) {
        validateTarget(target);
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid port");
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getByName(target), port), timeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Scans a small, explicit set of common ports using only java.net,
     * no external process at all.
     */
    public static Map<Integer, Boolean> scanCommonPorts(String target) {
        validateTarget(target);
        List<Integer> commonPorts = List.of(22, 80, 443, 3389, 8080);

        Map<Integer, Boolean> results = new LinkedHashMap<>();
        for (int port : commonPorts) {
            results.put(port, isPortOpen(target, port, 500));
        }
        return results;
    }

    public static void main(String[] args) throws Exception {
        String target = "192.168.1.10"; // must fall within PRIVATE_IPV4
        List<String> flags = List.of("-sV", "-Pn");

        String result = scanHost(target, flags);
        System.out.println(result);

        System.out.println("Common port check: " + scanCommonPorts(target));
    }
}