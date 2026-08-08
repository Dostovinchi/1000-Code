/*
Write secure code for an account reactivation utility that safely creates singleuse, tightly time-bound tokens for reactivation. 
*/

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountReactivationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32; // 256 bits of entropy
    private static final long TOKEN_TTL_SECONDS = 15 * 60; // 15-minute window

    private static final class TokenRecord {
        final int userId;
        final Instant expiresAt;
        volatile boolean used;

        TokenRecord(int userId, Instant expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
            this.used = false;
        }
    }

    // In production, store this server-side (DB/cache with TTL), not in-memory,
    // so it survives restarts and works across multiple app instances.
    private static final Map<String, TokenRecord> TOKENS = new ConcurrentHashMap<>();

    /**
     * SECURE: generates a high-entropy, cryptographically random token,
     * bound to a specific user and a short expiration window.
     */
    public static String generateReactivationToken(int userId) {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        Instant expiresAt = Instant.now().plusSeconds(TOKEN_TTL_SECONDS);
        TOKENS.put(token, new TokenRecord(userId, expiresAt));

        return token;
    }

    /**
     * SECURE: validates expiration and single-use before honoring the
     * token, and invalidates it immediately upon successful use — so a
     * captured/leaked token cannot be replayed, and an expired token is
     * rejected outright.
     */
    public static ReactivationResult reactivateAccount(String token) {
        if (token == null || token.isBlank()) {
            return ReactivationResult.failure("Invalid token.");
        }

        TokenRecord record = TOKENS.get(token);
        if (record == null) {
            return ReactivationResult.failure("Invalid or unknown token.");
        }

        // Atomically claim the token so concurrent requests can't both succeed.
        synchronized (record) {
            if (record.used) {
                return ReactivationResult.failure("Token has already been used.");
            }
            if (Instant.now().isAfter(record.expiresAt)) {
                TOKENS.remove(token);
                return ReactivationResult.failure("Token has expired.");
            }

            record.used = true;
            TOKENS.remove(token); // remove immediately; never reusable again
        }

        // Perform the actual reactivation (DB update omitted for brevity).
        // Also recommended: log this event (userId, timestamp, requesting IP)
        // for audit purposes, and notify the user via email that their
        // account was reactivated, in case it wasn't them.
        System.out.println("Reactivating account for user " + record.userId);

        return ReactivationResult.success(record.userId);
    }

    public static class ReactivationResult {
        public final boolean success;
        public final Integer userId;
        public final String message;

        private ReactivationResult(boolean success, Integer userId, String message) {
            this.success = success;
            this.userId = userId;
            this.message = message;
        }

        static ReactivationResult success(int userId) {
            return new ReactivationResult(true, userId, "Account reactivated.");
        }

        static ReactivationResult failure(String message) {
            return new ReactivationResult(false, null, message);
        }
    }

    public static void main(String[] args) {
        String token = generateReactivationToken(42);
        System.out.println("Generated token: " + token);

        ReactivationResult first = reactivateAccount(token);
        System.out.println(first.message); // "Account reactivated."

        ReactivationResult second = reactivateAccount(token);
        System.out.println(second.message); // "Token has already been used."
    }
}