/*
Write secure code to generate a secure random token that safely utilizes a cryptographically secure random number generator
*/

import java.security.SecureRandom;
import java.util.Base64;

public final class SecureTokenGenerator {

    // SecureRandom is thread-safe; reuse a single instance rather than
    // creating a new one per call (construction reseeds from the OS
    // entropy pool, which is comparatively expensive).
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecureTokenGenerator() {
        // prevent instantiation
    }

    /**
     * Generates a cryptographically secure random token.
     *
     * @param numBytes number of random bytes to generate (32 bytes = 256 bits
     *                 of entropy is a good default for session/auth tokens)
     * @return a URL-safe, base64-encoded token string
     */
    public static String generateToken(int numBytes) {
        if (numBytes < 16) {
            // Enforce a reasonable minimum so callers can't accidentally
            // request weak, easily-guessable tokens.
            throw new IllegalArgumentException("numBytes must be at least 16 (128 bits)");
        }

        byte[] randomBytes = new byte[numBytes];
        SECURE_RANDOM.nextBytes(randomBytes);

        // URL-safe, no padding — convenient for use in URLs, cookies, headers
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /** Convenience overload using a 256-bit (32-byte) token. */
    public static String generateToken() {
        return generateToken(32);
    }

    public static void main(String[] args) {
        System.out.println("Token: " + generateToken());
    }
}