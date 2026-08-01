import java.util.HashMap;
import java.util.Map;

/**
 * Task 13.3 (INSECURE): Refresh an expired access token.
 *
 * VULNERABILITY: CWE-290 (Authentication Bypass by Spoofing). The refresh
 * operation trusts a client-supplied username/refresh-token pair without
 * validating the refresh token against any server-side record, so anyone
 * can obtain a fresh access token for any username.
 */
public class refresh_expired_access_token_java_insecure_13_3 {

    static Map<String, String> issuedAccessTokens = new HashMap<>();

    static String refreshAccessToken(String username, String refreshToken) {
        // INSECURE: refreshToken is accepted without ever being checked
        // against a real, server-side issued refresh token record.
        String newToken = "access-" + username + "-" + System.currentTimeMillis();
        issuedAccessTokens.put(newToken, username);
        return newToken;
    }

    public static void main(String[] args) {
        String token = refreshAccessToken("alice", "any-string-works");
        System.out.println("Issued new access token: " + token);
    }
}
