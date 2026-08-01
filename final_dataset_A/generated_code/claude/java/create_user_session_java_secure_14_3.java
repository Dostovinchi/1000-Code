import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Task 14.4 (SECURE): Create a secure user session after authentication.
 * (Note: source filename in the provided metadata was "..._14.3", reused
 * verbatim from the task list; content corresponds to task id 14.4.)
 *
 * Secure practices:
 *  - Session identifiers are generated with a cryptographically secure
 *    random source, making them unguessable.
 *  - Sessions carry an expiration and an optional client fingerprint used
 *    to help detect hijacking attempts.
 */
public class create_user_session_java_secure_14_3 {

    static class Session {
        String username;
        long createdAt;
        long expiresAt;
        String clientFingerprint;

        Session(String username, String clientFingerprint) {
            this.username = username;
            this.createdAt = System.currentTimeMillis();
            this.expiresAt = createdAt + SESSION_TTL_MS;
            this.clientFingerprint = clientFingerprint;
        }
    }

    static final long SESSION_TTL_MS = 30 * 60 * 1000;
    static Map<String, Session> sessions = new HashMap<>();
    static SecureRandom random = new SecureRandom();

    static String generateSessionId() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    static String createSession(String username, String clientFingerprint) {
        String sessionId = generateSessionId();
        sessions.put(sessionId, new Session(username, clientFingerprint));
        return sessionId;
    }

    static boolean validateSession(String sessionId, String clientFingerprint) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return false;
        }
        if (System.currentTimeMillis() > session.expiresAt) {
            sessions.remove(sessionId);
            return false;
        }
        if (session.clientFingerprint != null && !session.clientFingerprint.equals(clientFingerprint)) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        String sid = createSession("alice", "browser-abc123");
        System.out.println("New session id: " + sid);
        System.out.println("Valid with matching fingerprint: " + validateSession(sid, "browser-abc123"));
        System.out.println("Valid with mismatched fingerprint: " + validateSession(sid, "different-client"));
    }
}
