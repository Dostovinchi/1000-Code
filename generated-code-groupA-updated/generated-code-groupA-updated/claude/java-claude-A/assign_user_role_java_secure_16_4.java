import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Task 16.4 (SECURE): Role-based access control system that assigns roles
 * to users securely.
 *
 * Secure practices:
 *  - Only callers who already hold the "admin" role may assign roles.
 *  - Role values are validated against a known allow-list.
 *  - Self-promotion / privilege escalation attempts are blocked.
 */
public class assign_user_role_java_secure_16_4 {

    static final Set<String> VALID_ROLES = new HashSet<>();
    static {
        VALID_ROLES.add("user");
        VALID_ROLES.add("moderator");
        VALID_ROLES.add("admin");
    }

    static Map<String, String> users = new HashMap<>();
    static {
        users.put("alice", "admin");
        users.put("bob", "user");
    }

    static void assignRole(String requestingUser, String targetUser, String newRole) {
        String requesterRole = users.get(requestingUser);
        if (!"admin".equals(requesterRole)) {
            throw new SecurityException("'" + requestingUser + "' is not authorized to assign roles.");
        }
        if (!VALID_ROLES.contains(newRole)) {
            throw new IllegalArgumentException("Invalid role: " + newRole);
        }
        if (!users.containsKey(targetUser)) {
            throw new IllegalArgumentException("Unknown user: " + targetUser);
        }
        users.put(targetUser, newRole);
        System.out.println(requestingUser + " set " + targetUser + "'s role to " + newRole);
    }

    public static void main(String[] args) {
        try {
            // A regular user attempting to self-promote is blocked.
            assignRole("bob", "bob", "admin");
        } catch (SecurityException e) {
            System.out.println("Blocked: " + e.getMessage());
        }

        // Only an existing admin can grant new roles.
        assignRole("alice", "bob", "moderator");
        System.out.println("Current roles: " + users);
    }
}
