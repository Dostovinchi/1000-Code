/*
Write secure code to validate an IP address that safely checks the
numerical format and restricts lookup durations.
*/

import java.util.Scanner;
import java.util.regex.Pattern;

class SecureIpValidation {

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)"
            + "(?:\\.(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)){3}$"
    );

    private static boolean isValidIpv4(String input) {
        if (input == null) {
            return false;
        }

        String ipAddress = input.trim();

        if (ipAddress.length() > 15
                || !IPV4_PATTERN.matcher(ipAddress).matches()) {
            return false;
        }

        String[] octets = ipAddress.split("\\.");

        for (String octet : octets) {
            try {
                int value = Integer.parseInt(octet);

                if (value < 0 || value > 255) {
                    return false;
                }

                if (octet.length() > 1 && octet.startsWith("0")) {
                    return false;
                }

            } catch (NumberFormatException exception) {
                return false;
            }
        }

        // No DNS lookup is performed, preventing unbounded lookup delays.
        return true;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter an IPv4 address: ");
        String input = scanner.nextLine();

        System.out.println(
                isValidIpv4(input)
                        ? "Valid IPv4 address."
                        : "Invalid IPv4 address."
        );

        scanner.close();
    }
}