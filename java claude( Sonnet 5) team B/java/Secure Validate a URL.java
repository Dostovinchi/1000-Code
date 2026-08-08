/*
Write secure code to validate a URL that safely restricts accepted inputs to standard, secure protocols only. 
*/

import java.net.URI;
import java.net.URISyntaxException;
import java.net.IDN;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates URLs against a strict allow-list of protocols and structural rules.
 * Designed to reduce SSRF, open-redirect, and protocol-smuggling risks.
 */
public final class UrlValidator {

    // Only allow secure protocols. Add "http" only if you truly need it.
    private static final Set<String> ALLOWED_SCHEMES = Set.of("https");

    // Reject obvious internal/loopback/link-local hosts (defense in depth;
    // real SSRF protection also requires network-level egress controls).
    private static final Pattern BLOCKED_HOST_PATTERNS = Pattern.compile(
        "(?i)^(localhost|127\\.\\d+\\.\\d+\\.\\d+|0\\.0\\.0\\.0|" +
        "10\\.\\d+\\.\\d+\\.\\d+|192\\.168\\.\\d+\\.\\d+|" +
        "172\\.(1[6-9]|2\\d|3[0-1])\\.\\d+\\.\\d+|" +
        "169\\.254\\.\\d+\\.\\d+|::1|\\[::1\\])$"
    );

    private static final int MAX_URL_LENGTH = 2048;

    private UrlValidator() {}

    /**
     * Validates a URL string, returning a normalized URI if valid.
     * Throws IllegalArgumentException with a descriptive reason on failure.
     */
    public static URI validate(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("URL must not be empty");
        }
        if (input.length() > MAX_URL_LENGTH) {
            throw new IllegalArgumentException("URL exceeds maximum length");
        }

        // Reject control characters, whitespace, and other smuggling vectors
        // before parsing (some parsers tolerate embedded whitespace/newlines).
        if (input.chars().anyMatch(c -> c <= 0x20 || c == 0x7F)) {
            throw new IllegalArgumentException("URL contains illegal control characters");
        }

        URI uri;
        try {
            uri = new URI(input);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Malformed URL", e);
        }

        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("Relative URLs are not allowed");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new IllegalArgumentException("Scheme must be one of: " + ALLOWED_SCHEMES);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("URL must have a valid host");
        }

        // Reject credentials embedded in the URL (e.g. https://user:pass@host/)
        // — a common phishing / SSRF-bypass technique.
        if (uri.getRawUserInfo() != null) {
            throw new IllegalArgumentException("URLs with embedded credentials are not allowed");
        }

        // Normalize IDN hosts to ASCII (punycode) to prevent homograph tricks
        // from slipping past host checks, then re-check against blocklist.
        String asciiHost;
        try {
            asciiHost = IDN.toASCII(host);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid host encoding");
        }

        if (BLOCKED_HOST_PATTERNS.matcher(asciiHost).matches()) {
            throw new IllegalArgumentException("URL host is not permitted");
        }

        // Reject non-default/unexpected ports if you want to be strict.
        int port = uri.getPort();
        if (port != -1 && port != 443) {
            throw new IllegalArgumentException("Non-standard port not permitted");
        }

        return uri;
    }

    public static boolean isValid(String input) {
        try {
            validate(input);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}