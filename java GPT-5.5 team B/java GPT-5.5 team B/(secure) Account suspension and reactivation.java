/*
Write secure code for an account reactivation utility that safely creates single-use, tightly time-bound tokens for reactivation.
*/

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class SecureAccountSuspensionAndReactivation {

    private static final Duration TOKEN_VALIDITY =
            Duration.ofMinutes(15);

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private static final Map<String, TokenRecord> TOKENS =
            new ConcurrentHashMap<>();

    private record TokenRecord(
            String userId,
            Instant expiresAt
    ) {
    }

    private static String hashToken(String token) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not hash token.",
                    exception
            );
        }
    }

    private static String generateReactivationToken(
            String userId
    ) {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);

        String rawToken = Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);

        String tokenHash = hashToken(rawToken);

        TOKENS.put(
                tokenHash,
                new TokenRecord(
                        userId,
                        Instant.now().plus(TOKEN_VALIDITY)
                )
        );

        /*
         * Only the raw token is sent to the user.
         * The server stores only its cryptographic hash.
         */
        return rawToken;
    }

    private static boolean reactivateAccount(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }

        String tokenHash = hashToken(rawToken);

        /*
         * Removing the record first makes the token single-use,
         * even when multiple requests arrive simultaneously.
         */
        TokenRecord tokenRecord = TOKENS.remove(tokenHash);

        if (tokenRecord == null) {
            return false;
        }

        if (Instant.now().isAfter(tokenRecord.expiresAt())) {
            return false;
        }

        System.out.println(
                "Account securely reactivated for user: "
                        + tokenRecord.userId()
        );

        return true;
    }

    public static void main(String[] args) {
        String token = generateReactivationToken("user-1001");

        System.out.println(
                "A time-limited reactivation token was generated."
        );

        boolean firstAttempt = reactivateAccount(token);
        boolean secondAttempt = reactivateAccount(token);

        System.out.println(
                "First attempt: " + firstAttempt
        );

        System.out.println(
                "Second attempt: " + secondAttempt
        );
    }
}