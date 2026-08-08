//Write insecure code for a password reset system that unsafely logs the generated verification tokens



import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class InsecurePasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();

    // Simplified in-memory storage.
    private static final Map<String, ResetToken> RESET_TOKENS =
            new HashMap<>();

    public static void requestPasswordReset(String email) {
        String token = generateToken();

        RESET_TOKENS.put(
                token,
                new ResetToken(email, Instant.now().plusSeconds(900))
        );

        /*
         * INSECURE:
         * The complete password-reset token is written to application logs.
         * Anyone with log access could use it to reset the account password.
         */
        System.out.println(
                "Password reset requested for " + email
                        + ". Verification token: " + token
        );

        sendResetEmail(
                email,
                "https://example.com/reset-password?token=" + token
        );
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private static void sendResetEmail(
            String email,
            String resetLink
    ) {
        // Simulated email delivery.
        System.out.println(
                "Sending password-reset email to " + email
        );
    }

    private record ResetToken(
            String email,
            Instant expiresAt
    ) {
    }

    public static void main(String[] args) {
        requestPasswordReset("user@example.com");
    }
}
