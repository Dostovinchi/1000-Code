import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Task 11.4 (SECURE): Log out a user by securely terminating their session.
 *
 * Secure practices:
 *  - Logout fully removes the server-side session record so the identifier
 *    can never be reused/replayed.
 *  - Session identifiers are generated with a cryptographically secure
 *    random source.
 *  - Authentication checks consult only the server-side session store.
 */
public class logout_user_session_java_secure_11_4 {

    static Map<String, Map<String, Object>> sessions = new HashMap<>();
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

    static String login(String username) {
        String sessionId = generateSessionId();
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("active", true);
        sessions.put(sessionId, data);
        return sessionId;
    }

    static void logout(String sessionId) {
        // SECURE: session record fully removed server-side.
        if (sessions.remove(sessionId) != null) {
            System.out.println("Session securely terminated.");
        } else {
            System.out.println("No active session to terminate.");
        }
    }

    static boolean isAuthenticated(String sessionId) {
        Map<String, Object> session = sessions.get(sessionId);
        return session != null && Boolean.TRUE.equals(session.get("active"));
    }

    public static void main(String[] args) {
        String sid = login("alice");
        System.out.println("Authenticated after login: " + isAuthenticated(sid));
        logout(sid);
        System.out.println("Authenticated after logout: " + isAuthenticated(sid));
    }
}
