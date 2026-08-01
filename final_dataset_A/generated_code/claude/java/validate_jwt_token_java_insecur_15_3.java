import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Task 15.3 (INSECURE): Validate a JWT token.
 *
 * VULNERABILITY: CWE-347 (Improper Verification of Cryptographic
 * Signature). The signature segment is decoded but never actually verified
 * against a known secret/key, and the "alg" header value (including
 * "none") is trusted blindly, along with the expiration claim being
 * ignored entirely. This allows anyone to forge a token.
 */
public class validate_jwt_token_java_insecur_15_3 {

    static String base64UrlDecode(String segment) {
        return new String(Base64.getUrlDecoder().decode(segment), StandardCharsets.UTF_8);
    }

    static String validateJwt(String token) {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            return null;
        }
        // INSECURE: header/payload are decoded but the signature (parts[2])
        // is never verified, and "exp" is never checked.
        String payload = base64UrlDecode(parts[1]);
        return payload;
    }

    public static void main(String[] args) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"user\":\"admin\",\"role\":\"admin\"}".getBytes(StandardCharsets.UTF_8));
        String forgedToken = header + "." + payload + ".";

        String result = validateJwt(forgedToken);
        System.out.println("Forged token accepted, claims: " + result);
    }
}
