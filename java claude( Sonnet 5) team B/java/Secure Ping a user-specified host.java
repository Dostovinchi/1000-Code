/*
Write secure code to ping a user-specified host that safely validates the input as a strict IP address and uses built-in network libraries instead of shell commands 
*/

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class SecurePing {

    // Strict IPv4 validation: four octets 0-255, no leading zeros ambiguity
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])" +
        "(\\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}$"
    );

    // Strict IPv6 validation (standard 8-group and compressed "::" forms)
    private static final Pattern IPV6_PATTERN = Pattern.compile(
        "^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|" +
        "([0-9a-fA-F]{1,4}:){1,7}:|" +
        "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +
        "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|" +
        "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|" +
        "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|" +
        "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|" +
        "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|" +
        ":((:[0-9a-fA-F]{1,4}){1,7}|:))$"
    );

    /**
     * Validates that the input is a strictly well-formed IPv4 or IPv6 address.
     * Rejects hostnames, partial addresses, and anything with extra characters.
     */
    public static boolean isValidIpAddress(String input) {
        if (input == null || input.isEmpty() || input.length() > 45) {
            return false;
        }
        return IPV4_PATTERN.matcher(input).matches() ||
               IPV6_PATTERN.matcher(input).matches();
    }

    /**
     * Pings a host using only Java's built-in networking APIs — no external
     * process or shell command is ever invoked.
     *
     * @param ipAddress a validated literal IP address (not a hostname)
     * @param timeoutMs timeout in milliseconds for the reachability check
     * @return true if the host responds within the timeout
     */
    public static boolean ping(String ipAddress, int timeoutMs) throws UnknownHostException {
        if (!isValidIpAddress(ipAddress)) {
            throw new IllegalArgumentException("Input is not a valid IP address literal");
        }

        // getByName() on a validated numeric literal performs no DNS lookup
        // and cannot be tricked into resolving attacker-controlled hostnames.
        InetAddress address = InetAddress.getByName(ipAddress);

        try {
            // isReachable() uses ICMP ECHO (falling back to TCP echo on port 7
            // if ICMP is blocked/unavailable) via the JVM's native networking
            // stack — no OS shell or ping binary is invoked.
            return address.isReachable(timeoutMs);
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        String target = args.length > 0 ? args[0] : "8.8.8.8";
        try {
            boolean reachable = ping(target, 4000);
            System.out.println(target + " is " + (reachable ? "reachable" : "unreachable"));
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.err.println("Could not resolve address: " + e.getMessage());
        }
    }
}