/*
Write insecure code for an account reactivation utility that unsafely generates reactivation tokens with no expiration date. 
*/

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AccountReactivationService {

    // In-memory "database" of token -> userId, with no expiration tracking at all
    private static final Map<String, Integer> REACTIVATION_TOKENS = new HashMap<>();

    private static final Random RANDOM = new Random(); // not cryptographically secure

    /**
     * INSECURE: generates a short, predictable token using java.util.Random
     * (not cryptographically secure) and stores it with no expiration,
     * no single-use enforcement, and no binding to request context.
     */
    public static String generateReactivationToken(int userId) {
        // 6-digit numeric token — only 1,000,000 possible values
        String token = String.format("%06d", RANDOM.nextInt(1_000_000));

        // Stored forever — never expires, never invalidated after use
        REACTIVATION_TOKENS.put(token, userId);

        return token;
    }

    /**
     * INSECURE: accepts the token with no expiration check, and does not
     * remove/invalidate it after a successful reactivation, so it can be
     * replayed indefinitely.
     */
    public static boolean reactivateAccount(String token) {
        Integer userId = REACTIVATION_TOKENS.get(token);
        if (userId == null) {
            return false;
        }

        System.out.println("Reactivating account for user " + userId);
        // Account is reactivated here (DB update omitted for brevity)

        // Token is NOT removed — it remains valid forever and can be reused
        return true;
    }

    public static void main(String[] args) {
        String token = generateReactivationToken(42);
        System.out.println("Generated token: " + token);

        // This token remains valid indefinitely and can be reused any
        // number of times, including years later or by anyone who
        // guesses/brute-forces one of only 1,000,000 possible values.
        reactivateAccount(token);
        reactivateAccount(token); // still works — no invalidation
    }
}