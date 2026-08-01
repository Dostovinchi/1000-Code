import java.util.HashMap;
import java.util.Map;
import java.util.Set;

 class SecureAssignUserRole {

    private final Map<String, String> userRoles = new HashMap<>();

    private static final Set<String> VALID_ROLES = Set.of(
            "admin",
            "moderator",
            "user"
    );

    public SecureAssignUserRole() {
        userRoles.put("alice", "admin");
        userRoles.put("bob", "user");
    }

    public boolean assignRole(String actingUser, String targetUser, String newRole) {

        String actingRole = userRoles.get(actingUser);

        if (actingRole == null || !"admin".equals(actingRole)) {
            System.out.println("Access denied: only administrators may assign roles.");
            return false;
        }

        if (!VALID_ROLES.contains(newRole)) {
            System.out.println("Invalid role.");
            return false;
        }

        if (!userRoles.containsKey(targetUser)) {
            System.out.println("Target user does not exist.");
            return false;
        }

        if (actingUser.equals(targetUser) && !"admin".equals(newRole)) {
            System.out.println("Administrators cannot downgrade their own role.");
            return false;
        }

        userRoles.put(targetUser, newRole);

        System.out.printf(
                "%s assigned role '%s' to %s%n",
                actingUser,
                newRole,
                targetUser
        );

        return true;
    }

    public void printRoles() {
        System.out.println("Current roles: " + userRoles);
    }

    public static void main(String[] args) {

        SecureAssignUserRole system = new SecureAssignUserRole();

        system.assignRole("bob", "bob", "admin");
        system.assignRole("alice", "bob", "moderator");

        system.printRoles();
    }
}