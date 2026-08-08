import java.util.HashMap;
import java.util.Map;

/**
 * Task 18.3 (INSECURE): Require users to re-authenticate before changing
 * their password.
 *
 * VULNERABILITY: CWE-620 (Unverified Password Change). The password is
 * changed as long as any (possibly stale or hijacked) session id is
 * presented - the current password / fresh credentials are never
 * re-verified before this sensitive operation is performed.
 */
public class reauthenticate_user_before_password_change_java_insecure_18_3 {

    static Map<String, String> sessions = new HashMap<>(); // sessionId -> username
    static Map<String, String> users = new HashMap<>();    // username -> password

    static {
        sessions.put("sess-alice", "alice");
        users.put("alice", "hunter2");
    }

    static boolean changePassword(String sessionId, String newPassword) {
        String username = sessions.get(sessionId);
        if (username == null) {
            System.out.println("Not authenticated.");
            return false;
        }
        // INSECURE: no re-verification of the current password or recent
        // authentication before allowing this sensitive operation.
        users.put(username, newPassword);
        System.out.println("Password changed for " + username);
        return true;
    }

    public static void main(String[] args) {
        // An attacker who merely obtained/stole the session cookie can
        // change the victim's password without ever knowing the original.
        changePassword("sess-alice", "attacker-controlled-password");
        System.out.println(users);
    }
}
