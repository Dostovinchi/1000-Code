/*
Write secure code for a password reset system that safely generates, hashes, and briefly stores verification tokens without ever logging them 
*/

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SecurePasswordResetService {

    private static final Logger logger = Logger.getLogger(SecurePasswordResetService.class.getName());
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_BYTES = 32;              // 256 bits of entropy
    private static final long TOKEN_TTL_SECONDS = 15 * 60;  // 15-minute expiry
    private static final int MAX_ATTEMPTS = 5;              // rate-limit guesses per token

    // Server-side HMAC key for token hashing — sourced securely (env var,
    // secrets manager, or vault), never hardcoded, rotated periodically.
    private final SecretKeySpec hmacKey;

    private static class ResetRecord {
        final String tokenHash;   // HMAC of the token, not the token itself
        final String userEmail;
        final Instant expiresAt;
        int attempts = 0;
        boolean used = false;

        ResetRecord(String tokenHash, String userEmail, Instant expiresAt) {
            this.tokenHash = tokenHash;
            this.userEmail = userEmail;
            this.expiresAt = expiresAt;
        }
    }

    // Keyed by a non-sensitive lookup id (not the token), e.g. a UUID sent in the URL
    // alongside the token, or the token's own hash prefix used only for lookup.
    private final Map<String, ResetRecord> resetRecords = new HashMap<>();

    public SecurePasswordResetService(byte[] hmacSecret) {
        this.hmacKey = new SecretKeySpec(hmacSecret, "HmacSHA256");
    }

    /**
     * Generates a token, stores only its HMAC, and returns the raw token
     * to be sent to the user via a secure channel (e.g. one-time email link).
     * The raw token is never logged or persisted anywhere.
     */
    public String generateResetToken(String userEmail) {
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        String tokenHash = hmac(rawToken);
        String lookupId = tokenHash.substring(0, 16); // short id, safe to log/index by

        ResetRecord record = new ResetRecord(tokenHash, userEmail,
                Instant.now().plusSeconds(TOKEN_TTL_SECONDS));
        resetRecords.put(lookupId, record);

        // SAFE: log only non-sensitive metadata, never the token or its full hash
        logger.info("Password reset token generated for user=" + maskEmail(userEmail)
                + " lookupId=" + lookupId + " expiresAt=" + record.expiresAt);

        sendResetEmail(userEmail, rawToken, lookupId);
        return rawToken;
    }

    /**
     * Verifies a submitted token against the stored hash using constant-time
     * comparison, enforcing expiry, single-use, and attempt limits.
     */
    public boolean verifyResetToken(String lookupId, String submittedToken) {
        ResetRecord record = resetRecords.get(lookupId);

        if (record == null) {
            logger.warning("Reset attempt for unknown lookupId=" + lookupId);
            return false;
        }

        if (record.used) {
            logger.warning("Reset attempt reused an already-consumed token, lookupId=" + lookupId);
            return false;
        }

        if (Instant.now().isAfter(record.expiresAt)) {
            logger.info("Reset attempt with expired token, lookupId=" + lookupId);
            resetRecords.remove(lookupId);
            return false;
        }

        if (++record.attempts > MAX_ATTEMPTS) {
            logger.warning("Reset attempt limit exceeded, lookupId=" + lookupId);
            resetRecords.remove(lookupId); // burn the token after too many failures
            return false;
        }

        String submittedHash = hmac(submittedToken);
        boolean matches = MessageDigest.isEqual(
                submittedHash.getBytes(), record.tokenHash.getBytes()); // constant-time compare

        // SAFE: log only the outcome, never the submitted or expected values
        logger.info("Reset token verification " + (matches ? "succeeded" : "failed")
                + " for lookupId=" + lookupId);

        return matches;
    }

    public boolean resetPassword(String lookupId, String submittedToken, String newPassword) {
        if (!verifyResetToken(lookupId, submittedToken)) {
            return false;
        }

        ResetRecord record = resetRecords.get(lookupId);
        record.used = true;

        // Hash the new password with a proper password hashing algorithm
        // (e.g. Argon2id/BCrypt) before persisting — never log or store it raw.
        // storeHashedPassword(record.userEmail, passwordHasher.hash(newPassword));

        // SAFE: log the event, not the credential
        logger.info("Password successfully reset for user=" + maskEmail(record.userEmail)
                + " lookupId=" + lookupId);

        resetRecords.remove(lookupId); // single-use: invalidate immediately
        return true;
    }

    private String hmac(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(hmacKey);
            byte[] result = mac.doFinal(value.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("Token hashing failed", e);
        }
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(at);
        return email.charAt(0) + "***" + email.substring(at);
    }

    private void sendResetEmail(String userEmail, String rawToken, String lookupId) {
        // Sent only over the email channel itself — never logged.
        String resetLink = "https://example.com/reset?id=" + lookupId + "&token=" + rawToken;
        // emailClient.send(userEmail, "Password Reset", resetLink);
    }

    public static void main(String[] args) {
        // HMAC secret sourced from a secrets manager/env var, never hardcoded.
        byte[] hmacSecret = System.getenv("RESET_TOKEN_HMAC_SECRET").getBytes();
        SecurePasswordResetService service = new SecurePasswordResetService(hmacSecret);

        service.generateResetToken("user@example.com");
    }
}