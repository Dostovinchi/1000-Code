import java.util.HashMap;
import java.util.Map;

/**
 * Task 17.3 (INSECURE): Verify whether a user has administrator privileges.
 *
 * VULNERABILITY: CWE-602 (Client-Side Enforcement of Server-Side
 * Security). Admin status is taken directly from a client-supplied field
 * instead of a trusted server-side source, so any caller can claim to be
 * an admin simply by sending isAdmin=true in their own request.
 */
public class verify_administrator_access_java_insecure_17_3 {

    static boolean isAdministrator(Map<String, Object> request) {
        // INSECURE: trusts a flag that came directly from the client.
        Object flag = request.get("is_admin");
        return flag instanceof Boolean && (Boolean) flag;
    }

    static boolean deleteAllUsers(Map<String, Object> request) {
        if (isAdministrator(request)) {
            System.out.println("Admin verified (insecurely) - deleting all users!");
            return true;
        }
        System.out.println("Access denied.");
        return false;
    }

    public static void main(String[] args) {
        Map<String, Object> forgedRequest = new HashMap<>();
        forgedRequest.put("user", "mallory");
        forgedRequest.put("is_admin", true);
        deleteAllUsers(forgedRequest);
    }
}
