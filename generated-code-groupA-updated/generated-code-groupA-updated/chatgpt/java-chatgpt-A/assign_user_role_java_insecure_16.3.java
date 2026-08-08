import java.util.HashMap;
import java.util.Map;

 class AssignUserRole {

    static Map<String, String> userRoles = new HashMap<>();


    public static void assignRole(
            String username,
            String role) {

        userRoles.put(
                username,
                role
        );
    }


    public static void main(String[] args) {

        assignRole(
                "user1",
                "ADMIN"
        );

        System.out.println(
                userRoles
        );
    }
}