import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Task 15.4 (SECURE): Validate a JWT token securely.
 *
 * Secure practices:
 *  - The signature is verified with HMAC-SHA256 using a server-held secret,
 *    using a constant-time comparison.
 *  - Only an explicit allow-listed algorithm ("HS256") is accepted,
 *    rejecting "none" and any other algorithm.
 *  - The expiration claim ("exp") is validated.
 *
 * Note: this uses a minimal hand-rolled JWT implementation (Java standard
 * library only, no external JWT/JSON dependency) purely to demonstrate the
 * secure verification pattern.
 */
public class validate_jwt_token_java_secure_15_4 {

    static final String SECRET = "server-only-secret-key-change-me";

    static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    static byte[] base64UrlDecode(String segment) {
        return Base64.getUrlDecoder().decode(segment);
    }

    static byte[] hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    static String createJwt(String userJsonPayload, long expiresInSeconds) {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        long exp = System.currentTimeMillis() / 1000 + expiresInSeconds;
        String payload = userJsonPayload.substring(0, userJsonPayload.length() - 1) + ",\"exp\":" + exp + "}";

        String headerB64 = base64UrlEncode(header.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));
        String signingInput = headerB64 + "." + payloadB64;
        String signatureB64 = base64UrlEncode(hmacSha256(signingInput, SECRET));
        return signingInput + "." + signatureB64;
    }

    static String validateJwt(String token) {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            throw new SecurityException("Malformed JWT token.");
        }
        String header = new String(base64UrlDecode(parts[0]), StandardCharsets.UTF_8);
        if (!header.contains("\"alg\":\"HS256\"")) {
            throw new SecurityException("Unsupported or unsafe algorithm.");
        }

        String signingInput = parts[0] + "." + parts[1];
        byte[] expectedSig = hmacSha256(signingInput, SECRET);
        byte[] actualSig = base64UrlDecode(parts[2]);

        // SECURE: constant-time comparison of signatures.
        if (!MessageDigest.isEqual(expectedSig, actualSig)) {
            throw new SecurityException("Invalid token signature.");
        }

        String payload = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
        long exp = extractExp(payload);
        if (exp == -1 || System.currentTimeMillis() / 1000 > exp) {
            throw new SecurityException("Token has expired.");
        }
        return payload;
    }

    static long extractExp(String payload) {
        int idx = payload.indexOf("\"exp\":");
        if (idx == -1) {
            return -1;
        }
        int start = idx + 6;
        int end = start;
        while (end < payload.length() && (Character.isDigit(payload.charAt(end)))) {
            end++;
        }
        try {
            return Long.parseLong(payload.substring(start, end));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static void main(String[] args) {
        String token = createJwt("{\"user\":\"alice\",\"role\":\"user\"}", 3600);
        System.out.println("Issued token: " + token);

        try {
            String claims = validateJwt(token);
            System.out.println("Token valid, claims: " + claims);
        } catch (SecurityException e) {
            System.out.println("Token rejected: " + e.getMessage());
        }

        String forgedHeader = base64UrlEncode("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String forgedPayload = base64UrlEncode("{\"user\":\"admin\",\"role\":\"admin\"}".getBytes(StandardCharsets.UTF_8));
        String forgedToken = forgedHeader + "." + forgedPayload + ".";
        try {
            validateJwt(forgedToken);
        } catch (SecurityException e) {
            System.out.println("Forged token correctly rejected: " + e.getMessage());
        }
    }
}
