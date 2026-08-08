//Write secure code for a password reset system that safely generates, hashes, and briefly stores verification tokens without ever logging them



import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SecurePasswordResetService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(10);

    /*
     * Stores only SHA-256 token hashes.
     * The raw verification token is never persisted or logged.
     */
    private final Map<String, StoredResetToken> tokenStore =
            new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordHasher passwordHasher;

    public SecurePasswordResetService(
            UserRepository userRepository,
            EmailService emailService,
            PasswordHasher passwordHasher
    ) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordHasher = passwordHasher;
    }

    public void requestPasswordReset(String email) {
        /*
         * Return the same response whether or not the account exists.
         * This prevents account enumeration.
         */
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            return;
        }

        invalidateExistingTokens(user.get().id());

        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        StoredResetToken storedToken = new StoredResetToken(
                user.get().id(),
                tokenHash,
                Instant.now().plus(TOKEN_LIFETIME),
                false
        );

        tokenStore.put(tokenHash, storedToken);

        /*
         * The raw token is sent directly to the user and is not stored.
         * Never print the token, reset URL, email body, or request parameters.
         */
        String resetLink =
                "https://example.com/reset-password?token="
                        + urlEncode(rawToken);

        emailService.sendPasswordResetLink(email, resetLink);
    }

    public boolean resetPassword(
            String suppliedToken,
            char[] newPassword
    ) {
        validatePassword(newPassword);

        String suppliedTokenHash = hashToken(suppliedToken);

        StoredResetToken storedToken =
                tokenStore.get(suppliedTokenHash);

        if (storedToken == null) {
            performDummyHash(newPassword);
            return false;
        }

        if (storedToken.used()
                || Instant.now().isAfter(storedToken.expiresAt())) {

            tokenStore.remove(suppliedTokenHash);
            performDummyHash(newPassword);
            return false;
        }

        /*
         * Constant-time comparison prevents timing differences from revealing
         * whether part of a token hash matched.
         */
        if (!constantTimeEquals(
                storedToken.tokenHash(),
                suppliedTokenHash
        )) {
            performDummyHash(newPassword);
            return false;
        }

        String passwordHash = passwordHasher.hash(newPassword);

        try {
            userRepository.updatePassword(
                    storedToken.userId(),
                    passwordHash
            );

            /*
             * One-time use: delete immediately after a successful reset.
             */
            tokenStore.remove(suppliedTokenHash);

            /*
             * Invalidate all other reset tokens and active sessions.
             */
            invalidateExistingTokens(storedToken.userId());
            userRepository.invalidateSessions(storedToken.userId());

            return true;

        } finally {
            java.util.Arrays.fill(newPassword, '\0');
        }
    }

    public void removeExpiredTokens() {
        Instant now = Instant.now();

        tokenStore.entrySet().removeIf(entry ->
                entry.getValue().used()
                        || now.isAfter(entry.getValue().expiresAt())
        );
    }

    private static String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        try {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(randomBytes);
        } finally {
            java.util.Arrays.fill(randomBytes, (byte) 0);
        }
    }

    private static String hashToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Reset token is required."
            );
        }

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] tokenBytes =
                    token.getBytes(StandardCharsets.UTF_8);

            try {
                byte[] hash = digest.digest(tokenBytes);

                return Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(hash);
            } finally {
                java.util.Arrays.fill(tokenBytes, (byte) 0);
            }

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to hash reset token.",
                    exception
            );
        }
    }

    private void invalidateExistingTokens(String userId) {
        tokenStore.entrySet().removeIf(entry ->
                entry.getValue().userId().equals(userId)
        );
    }

    private static boolean constantTimeEquals(
            String expected,
            String actual
    ) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                actual.getBytes(StandardCharsets.US_ASCII)
        );
    }

    private static void validatePassword(char[] password) {
        if (password == null || password.length < 12) {
            throw new IllegalArgumentException(
                    "Password must contain at least 12 characters."
            );
        }
    }

    private void performDummyHash(char[] password) {
        /*
         * Reduces observable timing differences for invalid tokens.
         */
        passwordHasher.hash(password.clone());
    }

    private static String urlEncode(String value) {
        return java.net.URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }

    private record StoredResetToken(
            String userId,
            String tokenHash,
            Instant expiresAt,
            boolean used
    ) {
    }

    public record User(
            String id,
            String email
    ) {
    }

    public interface UserRepository {
        Optional<User> findByEmail(String email);

        void updatePassword(
                String userId,
                String passwordHash
        );

        void invalidateSessions(String userId);
    }

    public interface EmailService {
        void sendPasswordResetLink(
                String email,
                String resetLink
        );
    }

    public interface PasswordHasher {
        /*
         * Implement with Argon2id, bcrypt, or scrypt.
         */
        String hash(char[] password);
    }
}