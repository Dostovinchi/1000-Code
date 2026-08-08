import java.util.HashMap;
import java.util.Map;

/**
 * Task 14.3 (INSECURE): Create a user session after authentication.
 *
 * VULNERABILITY: CWE-330 (Use of Insufficiently Random Values). The session
 * identifier is derived from predictable data (username + incrementing
 * counter) instead of a cryptographically secure random source, making
 * session IDs guessable/enumerable (session hijacking).
 */
public class create_user_session_java_insecure_14_3 {

    static Map<String, Map<String, Object>> sessions = new HashMap<>();
    static int counter = 0;

    static String createSession(String username) {
        counter++;
        // INSECURE: predictable, trivially guessable session id.
        String sessionId = username + "-" + counter;
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        sessions.put(sessionId, data);
        return sessionId;
    }

    public static void main(String[] args) {
        for (String name : new String[]{"alice", "bob", "carol"}) {
            String sid = createSession(name);
            System.out.println("Session for " + name + ": " + sid);
        }
    }
}
