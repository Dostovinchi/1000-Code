import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Task 18.4 (SECURE): Require users to re-authenticate before changing
 * their password, securely.
 *
 * Secure practices:
 *  - The caller must supply and correctly verify their *current* password
 *    (constant-time comparison against a salted PBKDF2 hash) before a
 *    change is allowed.
 *  - Sensitive operations additionally require the session to be "fresh"
 *    (recently authenticated), rejecting stale/old sessions.
 *  - Failed re-authentication attempts are rejected, not silently ignored.
 */
public class reauthenticate_user_before_password_change_java_secure_18_4 {

    static class Session {
        String username;
        long authenticatedAt;

        Session(String username, long authenticatedAt) {
            this.username = username;
            this.authenticatedAt = authenticatedAt;
        }
    }

    static class Credentials {
        byte[] salt;
        byte[] hash;

        Credentials(byte[] salt, byte[] hash) {
            this.salt = salt;
            this.hash = hash;
        }
    }

    static final long REAUTH_FRESHNESS_MS = 5 * 60 * 1000;
    static SecureRandom random = new SecureRandom();
    static Map<String, Session> sessions = new HashMap<>();
    static Map<String, Credentials> users = new HashMap<>();

    static {
        sessions.put("sess-alice", new Session("alice", System.currentTimeMillis()));
        users.put("alice", hashPassword("hunter2"));
    }

    static byte[] generateSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    static Credentials hashPassword(String password) {
        byte[] salt = generateSalt();
        return new Credentials(salt, derive(password, salt));
    }

    static byte[] derive(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 200_000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean changePassword(String sessionId, String currentPassword, String newPassword) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            System.out.println("Not authenticated.");
            return false;
        }
        if (System.currentTimeMillis() - session.authenticatedAt > REAUTH_FRESHNESS_MS) {
            System.out.println("Session too old for a sensitive operation; please log in again.");
            return false;
        }

        Credentials creds = users.get(session.username);
        byte[] candidate = derive(currentPassword, creds.salt);

        // SECURE: current password must be correctly re-verified in
        // constant time before allowing the change.
        if (!MessageDigest.isEqual(candidate, creds.hash)) {
            System.out.println("Re-authentication failed: current password is incorrect.");
            return false;
        }

        users.put(session.username, hashPassword(newPassword));
        System.out.println("Password changed for " + session.username);
        return true;
    }

    public static void main(String[] args) {
        System.out.println("Wrong current password: " + changePassword("sess-alice", "wrong-pass", "new-secret"));
        System.out.println("Correct current password: " + changePassword("sess-alice", "hunter2", "new-secret"));
    }
}
