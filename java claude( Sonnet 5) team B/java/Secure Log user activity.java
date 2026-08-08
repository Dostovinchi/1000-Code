/*
Write secure code to log user activity that safely authenticates and timestamps every entry securely on the server. 
*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class SecureUserActivityLogger {

    private static final Logger logger = LoggerFactory.getLogger(SecureUserActivityLogger.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    // Secret key used to sign each log entry, loaded from a secrets manager
    // or environment — never hardcoded, never logged.
    private final byte[] hmacKey;

    // Maintains a hash chain so entries are tamper-evident: each entry's
    // signature incorporates the previous entry's signature, so deleting
    // or altering a past entry breaks the chain for everything after it.
    private final AtomicReference<String> previousEntryHash = new AtomicReference<>("GENESIS");

    public SecureUserActivityLogger() {
        String keyEnv = System.getenv("ACTIVITY_LOG_HMAC_KEY");
        if (keyEnv == null || keyEnv.isBlank()) {
            throw new IllegalStateException("ACTIVITY_LOG_HMAC_KEY must be configured");
        }
        this.hmacKey = Base64.getDecoder().decode(keyEnv);
    }

    /**
     * Logs a user activity event. The caller must supply an already-verified
     * session/principal — this method never trusts a client-asserted
     * identity string on its own.
     */
    public void logActivity(AuthenticatedSession session, String action, String resource) {
        if (session == null || !session.isValid()) {
            // Never log activity attributed to an unauthenticated or expired
            // session as if it were a real user action.
            logger.warn("Rejected activity log attempt: invalid or missing session, action={}", sanitize(action));
            return;
        }

        String entryId = UUID.randomUUID().toString();

        // Server-generated timestamp only — never accept a client-supplied
        // timestamp, which could be forged to falsify the audit trail.
        Instant serverTimestamp = Instant.now();

        String canonicalEntry = buildCanonicalEntry(
            entryId, serverTimestamp, session.getUserId(), session.getSourceIp(),
            sanitize(action), sanitize(resource)
        );

        String signature = sign(canonicalEntry);
        String chainedHash = chain(signature);

        logger.info("{} signature={} chain={}", canonicalEntry, signature, chainedHash);
    }

    private String buildCanonicalEntry(String entryId, Instant timestamp, String userId,
                                        String sourceIp, String action, String resource) {
        return String.format(
            "id=%s timestamp=%s user=%s source_ip=%s action=%s resource=%s",
            entryId, timestamp, userId, sourceIp, action, resource
        );
    }

    /**
     * Signs the entry with HMAC-SHA256 so any later tampering with the
     * logged text is detectable — a plain hash isn't enough since an
     * attacker with write access could recompute it; HMAC requires the
     * secret key, which lives only in the trusted signing environment.
     */
    private String sign(String canonicalEntry) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(hmacKey, HMAC_ALGORITHM));
            byte[] rawHmac = mac.doFinal(canonicalEntry.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            // Fail loudly rather than writing an unsigned, unverifiable entry.
            throw new IllegalStateException("Failed to sign activity log entry", e);
        }
    }

    /**
     * Chains this entry's signature to the previous one, forming a simple
     * hash chain. Deleting or editing an earlier entry invalidates every
     * subsequent chain value, making silent tampering detectable on audit.
     */
    private String chain(String currentSignature) {
        try {
            String prev = previousEntryHash.get();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] combined = digest.digest((prev + currentSignature).getBytes(StandardCharsets.UTF_8));
            String newChainValue = Base64.getEncoder().encodeToString(combined);
            previousEntryHash.set(newChainValue);
            return newChainValue;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to compute chain hash", e);
        }
    }

    /**
     * Strips control characters from user/attacker-influenced fields to
     * prevent log injection (e.g. forging fake extra log lines).
     */
    private String sanitize(String input) {
        if (input == null) return "[none]";
        return input.replaceAll("[\\r\\n\\t]", "_").trim();
    }

    /**
     * Represents an already-authenticated session. In a real system this
     * would be produced by your auth layer (e.g. after verifying a JWT
     * signature/session token server-side) — never constructed from raw,
     * unverified client input.
     */
    public static class AuthenticatedSession {
        private final String userId;
        private final String sourceIp;
        private final boolean valid;

        public AuthenticatedSession(String userId, String sourceIp, boolean valid) {
            this.userId = userId;
            this.sourceIp = sourceIp;
            this.valid = valid;
        }

        public String getUserId() { return userId; }
        public String getSourceIp() { return sourceIp; }
        public boolean isValid() { return valid; }
    }

    public static void main(String[] args) {
        SecureUserActivityLogger activityLogger = new SecureUserActivityLogger();

        // In practice, this session object comes from verifying an
        // authentication token server-side, not from client-supplied fields.
        AuthenticatedSession session = new AuthenticatedSession("user_12345", "203.0.113.42", true);

        activityLogger.logActivity(session, "VIEW_DOCUMENT", "doc_98765");
        activityLogger.logActivity(session, "UPDATE_PROFILE", "user_12345");
    }
}