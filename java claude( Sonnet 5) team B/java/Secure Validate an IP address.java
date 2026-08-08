/*
Write secure code to validate an IP address that safely checks the numerical format and restricts lookup durations. 
*/

import java.util.regex.Pattern;
import java.util.regex.Matcher;

// ---------------------------------------------------------------------------
// Secure IP address validation
// ---------------------------------------------------------------------------
//
// Design decisions:
// - Pure string/numeric parsing — no dynamic code execution (no script
//   engines, no reflection), closing off the injection class of bug
//   entirely rather than trying to sanitize around it.
// - No use of InetAddress.getByName()/getAllByName() for validation.
//   Those methods perform DNS resolution for hostnames and, depending
//   on the input and system resolver configuration, can block for a
//   long time with no built-in timeout — an attacker could supply input
//   that causes slow/hanging lookups (a lightweight DoS vector) or
//   trigger unwanted outbound network requests as a side effect of
//   "just validating a string."
// - Strict numeric bounds checking (0-255 per IPv4 octet, valid hextet
//   ranges for IPv6) with no leading-zero ambiguity (e.g. "192.168.01.1"
//   is rejected — leading zeros in octets have historically been
//   interpreted inconsistently, including as octal, across different
//   parsers, which has enabled SSRF/allow-list bypass bugs).
// - Bounded input length checked up front, before any regex is applied,
//   as defense-in-depth against pathological input to the regex engine.

public final class SecureIpValidator {

    private static final int MAX_INPUT_LENGTH = 45; // longest valid IPv6 literal

    // IPv4: four 1-3 digit groups, no leading zeros (except a lone "0"),
    // each 0-255, separated by dots. Anchored, no ambiguity.
    private static final Pattern IPV4_OCTET =
        Pattern.compile("^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$");

    private SecureIpValidator() {
        // utility class
    }

    /**
     * Validates that `input` is a well-formed IPv4 address, checked
     * purely as a string/numeric format — never resolved, never
     * executed as code.
     */
    public static boolean isValidIpv4(String input) {
        if (input == null || input.isEmpty() || input.length() > MAX_INPUT_LENGTH) {
            return false;
        }

        String[] octets = input.split("\\.", -1); // -1 keeps trailing empty strings
        if (octets.length != 4) {
            return false;
        }

        for (String octet : octets) {
            if (!isValidIpv4Octet(octet)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidIpv4Octet(String octet) {
        if (octet.isEmpty() || octet.length() > 3) {
            return false;
        }
        // Reject ambiguous leading zeros (e.g. "01", "007") — only a
        // single "0" is valid on its own. Prevents octal-interpretation
        // confusion across different downstream consumers of the value.
        if (octet.length() > 1 && octet.charAt(0) == '0') {
            return false;
        }
        Matcher matcher = IPV4_OCTET.matcher(octet);
        return matcher.matches();
    }

    /**
     * Validates that `input` is a well-formed IPv6 address (including
     * "::" zero-compression), again as pure string/numeric parsing.
     */
    public static boolean isValidIpv6(String input) {
        if (input == null || input.isEmpty() || input.length() > MAX_INPUT_LENGTH) {
            return false;
        }

        // Strip an optional zone index (e.g. "%eth0") before structural
        // validation — zone indices are locally scoped and not part of
        // the numeric address itself.
        String candidate = input;
        int zoneIndex = candidate.indexOf('%');
        if (zoneIndex != -1) {
            candidate = candidate.substring(0, zoneIndex);
        }

        String[] halves = candidate.split("::", -1);
        if (halves.length > 2) {
            return false; // more than one "::" is never valid
        }

        boolean hasCompression = candidate.contains("::");
        String[] groups;

        if (hasCompression) {
            String left = halves[0];
            String right = halves.length > 1 ? halves[1] : "";
            String[] leftGroups = left.isEmpty() ? new String[0] : left.split(":");
            String[] rightGroups = right.isEmpty() ? new String[0] : right.split(":");

            if (leftGroups.length + rightGroups.length >= 8) {
                return false; // "::" must represent at least one group
            }
            groups = concat(leftGroups, rightGroups);
        } else {
            groups = candidate.split(":", -1);
            if (groups.length != 8) {
                return false;
            }
        }

        for (String group : groups) {
            if (!isValidHextet(group)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidHextet(String group) {
        if (group.isEmpty() || group.length() > 4) {
            return false;
        }
        for (int i = 0; i < group.length(); i++) {
            char c = group.charAt(i);
            boolean isHexDigit = (c >= '0' && c <= '9')
                || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F');
            if (!isHexDigit) {
                return false;
            }
        }
        return true;
    }

    private static String[] concat(String[] a, String[] b) {
        String[] result = new String[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static boolean isValidIpAddress(String input) {
        return isValidIpv4(input) || isValidIpv6(input);
    }

    public static void main(String[] args) {
        String[] testInputs = {
            "192.168.1.1",       // valid
            "255.255.255.255",   // valid
            "999.1.1.1",         // invalid: out of range
            "192.168.01.1",      // invalid: ambiguous leading zero
            "192.168.1",         // invalid: too few octets
            "::1",                // valid IPv6 loopback
            "2001:db8::ff00:42:8329", // valid IPv6
            "2001:db8:::1",       // invalid: double "::"
            "not.an.ip.address",  // invalid
            "'; malicious_code(); var x='" // invalid, and never executed — just fails cleanly
        };

        for (String ip : testInputs) {
            System.out.printf("%-35s -> %s%n", ip, isValidIpAddress(ip));
        }
    }
}