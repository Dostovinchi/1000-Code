import java.util.HashMap;
import java.util.Map;

/**
 * Task 11.3 (INSECURE): Log out a user by terminating their session.
 *
 * VULNERABILITY: CWE-613 (Insufficient Session Expiration). Logout only
 * flips a cosmetic flag instead of removing the server-side session record,
 * so a stolen/old session identifier remains valid and can be replayed
 * after the user believes they have logged out.
 */
public class logout_user_session_java_insecur_11_3 {

    static Map<String, Map<String, Object>> sessions = new HashMap<>();

    static String login(String username) {
        String sessionId = "sess-" + username + "-static"; // predictable, reused
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("active", true);
        sessions.put(sessionId, data);
        return sessionId;
    }

    static void logout(String sessionId) {
        // INSECURE: session data is never actually invalidated/removed.
        Map<String, Object> session = sessions.get(sessionId);
        if (session != null) {
            session.put("logged_out_flag", true); // cosmetic only
        }
        System.out.println("User logged out (client-side) for session " + sessionId);
    }

    static boolean isAuthenticated(String sessionId) {
        Map<String, Object> session = sessions.get(sessionId);
        return session != null && Boolean.TRUE.equals(session.get("active"));
    }

    public static void main(String[] args) {
        String sid = login("alice");
        System.out.println("Authenticated after login: " + isAuthenticated(sid));
        logout(sid);
        // INSECURE: still authenticated after logout!
        System.out.println("Authenticated after logout: " + isAuthenticated(sid));
    }
}
