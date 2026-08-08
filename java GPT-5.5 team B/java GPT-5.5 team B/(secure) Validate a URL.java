/*
Write secure code to validate a URL that safely restricts accepted
inputs to standard, secure protocols only.
*/

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.util.Scanner;

class SecureUrlValidation {

    private static boolean isPublicAddress(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return false;
        }

        byte[] bytes = address.getAddress();

        if (address instanceof Inet4Address) {
            int first = bytes[0] & 0xFF;
            int second = bytes[1] & 0xFF;

            if (first == 0
                    || first == 10
                    || first == 127
                    || (first == 100 && second >= 64 && second <= 127)
                    || (first == 169 && second == 254)
                    || (first == 172 && second >= 16 && second <= 31)
                    || (first == 192 && second == 168)) {
                return false;
            }
        }

        if (address instanceof Inet6Address) {
            int first = bytes[0] & 0xFF;
            int second = bytes[1] & 0xFF;

            // Unique-local and link-local IPv6 ranges.
            if ((first & 0xFE) == 0xFC
                    || (first == 0xFE && (second & 0xC0) == 0x80)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidUrl(String input) {
        try {
            URI uri = new URI(input.trim());

            // Accept HTTPS only.
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                return false;
            }

            String host = uri.getHost();

            if (host == null
                    || host.isBlank()
                    || uri.getUserInfo() != null
                    || uri.getFragment() != null) {
                return false;
            }

            InetAddress[] addresses = InetAddress.getAllByName(host);

            if (addresses.length == 0) {
                return false;
            }

            for (InetAddress address : addresses) {
                if (!isPublicAddress(address)) {
                    return false;
                }
            }

            return true;

        } catch (Exception exception) {
            return false;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter an HTTPS URL: ");
        String input = scanner.nextLine();

        System.out.println(
                isValidUrl(input)
                        ? "Valid and permitted URL."
                        : "Invalid or prohibited URL."
        );

        scanner.close();
    }
}