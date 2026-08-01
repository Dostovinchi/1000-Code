import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Task 13.4 (SECURE): Refresh an expired access token securely.
 *
 * Secure practices:
 *  - Refresh tokens are opaque, cryptographically random, and validated
 *    against a server-side store before use.
 *  - Expired or unknown refresh tokens are rejected.
 *  - Refresh tokens are rotated (single-use) on each successful refresh,
 *    preventing replay of a stolen token.
 */
public class refresh_expired_access_token_java_secure_13_4 {

    static class TokenRecord {
        String username;
        long expiresAt;

        TokenRecord(String username, long expiresAt) {
            this.username = username;
            this.expiresAt = expiresAt;
        }
    }

    static final long ACCESS_TOKEN_TTL_MS = 15 * 60 * 1000;
    static final long REFRESH_TOKEN_TTL_MS = 7L * 24 * 60 * 60 * 1000;

    static Map<String, TokenRecord> accessTokens = new HashMap<>();
    static Map<String, TokenRecord> refreshTokens = new HashMap<>();
    static SecureRandom random = new SecureRandom();

    static String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    static String[] issueTokens(String username) {
        String accessToken = generateToken();
        String refreshToken = generateToken();
        long now = System.currentTimeMillis();
        accessTokens.put(accessToken, new TokenRecord(username, now + ACCESS_TOKEN_TTL_MS));
        refreshTokens.put(refreshToken, new TokenRecord(username, now + REFRESH_TOKEN_TTL_MS));
        return new String[]{accessToken, refreshToken};
    }

    static String[] refreshAccessToken(String refreshToken) {
        TokenRecord record = refreshTokens.get(refreshToken);
        if (record == null) {
            throw new SecurityException("Invalid refresh token.");
        }
        if (System.currentTimeMillis() > record.expiresAt) {
            refreshTokens.remove(refreshToken);
            throw new SecurityException("Refresh token has expired.");
        }
        String username = record.username;
        // SECURE: rotate refresh token immediately to prevent replay.
        refreshTokens.remove(refreshToken);
        return issueTokens(username);
    }

    public static void main(String[] args) {
        String[] initial = issueTokens("alice");
        System.out.println("Initial access token: " + initial[0]);

        try {
            String[] refreshed = refreshAccessToken(initial[1]);
            System.out.println("Refreshed access token: " + refreshed[0]);
        } catch (SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            refreshAccessToken(initial[1]);
        } catch (SecurityException e) {
            System.out.println("Replay correctly rejected: " + e.getMessage());
        }
    }
}
