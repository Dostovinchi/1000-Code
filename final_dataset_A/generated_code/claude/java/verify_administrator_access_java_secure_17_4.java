import java.util.HashMap;
import java.util.Map;

/**
 * Task 17.4 (SECURE): Verify whether a user has administrator privileges
 * securely.
 *
 * Secure practices:
 *  - Privilege is looked up from a trusted, server-side role store keyed by
 *    an authenticated session id, never from client-supplied fields.
 *  - Unknown/unauthenticated sessions are denied by default (fail closed).
 */
public class verify_administrator_access_java_secure_17_4 {

    static Map<String, String> sessions = new HashMap<>();
    static Map<String, String> userRoles = new HashMap<>();

    static {
        sessions.put("sess-alice", "alice");
        sessions.put("sess-mallory", "mallory");

        userRoles.put("alice", "admin");
        userRoles.put("mallory", "user");
    }

    static boolean isAdministrator(String sessionId) {
        String username = sessions.get(sessionId);
        if (username == null) {
            return false; // fail closed for unknown/unauthenticated sessions
        }
        String role = userRoles.getOrDefault(username, "user");
        return "admin".equals(role);
    }

    static boolean deleteAllUsers(String sessionId) {
        if (isAdministrator(sessionId)) {
            System.out.println("Admin verified (securely) - deleting all users!");
            return true;
        }
        System.out.println("Access denied: administrator privileges required.");
        return false;
    }

    public static void main(String[] args) {
        deleteAllUsers("sess-mallory");
        deleteAllUsers("sess-alice");
        deleteAllUsers("sess-does-not-exist");
    }
}
