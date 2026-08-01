import java.util.HashMap;
import java.util.Map;

/**
 * Task 16.3 (INSECURE): Role-based access control system that assigns
 * roles to users.
 *
 * VULNERABILITY: CWE-269 (Improper Privilege Management). Any caller can
 * assign any role - including "admin" - to any user, with no check that
 * the requester itself already holds sufficient privilege. This allows
 * trivial privilege escalation.
 */
public class assign_user_role_java_insecure_16_3 {

    static Map<String, String> users = new HashMap<>();

    static {
        users.put("alice", "user");
        users.put("bob", "user");
    }

    static void assignRole(String requestingUser, String targetUser, String newRole) {
        // INSECURE: no authorization check on requestingUser at all.
        users.put(targetUser, newRole);
        System.out.println(requestingUser + " set " + targetUser + "'s role to " + newRole);
    }

    public static void main(String[] args) {
        // A regular, unprivileged user grants themselves the admin role.
        assignRole("bob", "bob", "admin");
        System.out.println("Current roles: " + users);
    }
}
