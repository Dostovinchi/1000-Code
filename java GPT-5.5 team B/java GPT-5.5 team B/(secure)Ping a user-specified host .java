//Write secure code to ping a user-specified host that safely validates the input as a strict IP address and uses built-in network libraries instead of shell commands


import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public final class SecurePingService {

    private static final int TIMEOUT_MILLISECONDS = 3_000;
    private static final int TTL = 64;

    private SecurePingService() {
    }

    public static boolean ping(String userInput) throws IOException {
        Inet4Address target = parseStrictIpv4Address(userInput);

        /*
         * Uses Java's networking API directly.
         * No shell, Runtime.exec(), ProcessBuilder, or command concatenation.
         */
        NetworkInterface networkInterface = findUsableInterface();

        return target.isReachable(
                networkInterface,
                TTL,
                TIMEOUT_MILLISECONDS
        );
    }

    private static Inet4Address parseStrictIpv4Address(String input) {
        if (input == null) {
            throw new IllegalArgumentException("IP address is required.");
        }

        String value = input.trim();

        /*
         * Reject hostnames, whitespace tricks, command separators,
         * IPv6 zone identifiers, and noncanonical IPv4 formats.
         */
        if (!value.matches(
                "(?:0|[1-9]\\d{0,2})"
                        + "(?:\\.(?:0|[1-9]\\d{0,2})){3}"
        )) {
            throw new IllegalArgumentException(
                    "A canonical IPv4 address is required."
            );
        }

        String[] octets = value.split("\\.", -1);
        byte[] addressBytes = new byte[4];

        for (int index = 0; index < octets.length; index++) {
            int octet;

            try {
                octet = Integer.parseInt(octets[index]);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        "Invalid IPv4 address.",
                        exception
                );
            }

            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException(
                        "Every IPv4 octet must be between 0 and 255."
                );
            }

            addressBytes[index] = (byte) octet;
        }

        try {
            InetAddress address = InetAddress.getByAddress(addressBytes);

            if (!(address instanceof Inet4Address ipv4Address)) {
                throw new IllegalArgumentException(
                        "An IPv4 address is required."
                );
            }

            blockUnsafeDestinations(ipv4Address);
            return ipv4Address;

        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Invalid IPv4 address.",
                    exception
            );
        }
    }

    private static void blockUnsafeDestinations(Inet4Address address) {
        /*
         * Optional SSRF protection. Adjust this allow/block policy
         * according to the application's intended network scope.
         */
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {

            throw new SecurityException(
                    "Private, local, and multicast addresses are not allowed."
            );
        }

        byte[] bytes = address.getAddress();
        int first = Byte.toUnsignedInt(bytes[0]);
        int second = Byte.toUnsignedInt(bytes[1]);

        // Carrier-grade NAT: 100.64.0.0/10
        if (first == 100 && second >= 64 && second <= 127) {
            throw new SecurityException(
                    "Carrier-grade NAT addresses are not allowed."
            );
        }

        // Reserved/documentation ranges that should not be contacted.
        if (first == 0
                || first >= 224
                || (first == 192 && second == 0)
                || (first == 198
                    && (second == 18 || second == 19))) {

            throw new SecurityException(
                    "Reserved IP addresses are not allowed."
            );
        }
    }

    private static NetworkInterface findUsableInterface()
            throws IOException {

        Enumeration<NetworkInterface> interfaces =
                NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface candidate = interfaces.nextElement();

            if (candidate.isUp()
                    && !candidate.isLoopback()
                    && !candidate.isVirtual()) {
                return candidate;
            }
        }

        /*
         * A null interface lets the Java implementation select
         * an appropriate local interface.
         */
        return null;
    }

    public static void main(String[] args) {
        try {
            String userProvidedIp = "8.8.8.8";
            boolean reachable = ping(userProvidedIp);

            System.out.println(
                    reachable
                            ? "Host is reachable."
                            : "Host did not respond within the timeout."
            );

        } catch (IllegalArgumentException | SecurityException exception) {
            System.err.println("Rejected destination: "
                    + exception.getMessage());

        } catch (IOException exception) {
            System.err.println("Network reachability check failed.");
        }
    }
}