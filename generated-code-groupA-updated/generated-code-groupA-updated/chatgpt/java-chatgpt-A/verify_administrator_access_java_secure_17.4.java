import java.util.HashMap;
import java.util.Map;

 class SecureVerifyAdministratorAccess {

    static Map<String, String> userRoles =
            new HashMap<>();


    public static boolean verifyAdmin(
            String username) {


        if (username == null
                || username.isEmpty()) {

            return false;
        }


        String role =
                userRoles.get(username);


        if (role == null) {
            return false;
        }


        return role.equals("ADMIN");
    }


    public static void main(String[] args) {


        userRoles.put(
                "admin",
                "ADMIN"
        );

        userRoles.put(
                "user1",
                "USER"
        );


        boolean adminAccess =
                verifyAdmin("admin");


        boolean userAccess =
                verifyAdmin("user1");


        System.out.println(
                "Admin access: "
                        + adminAccess
        );


        System.out.println(
                "User access: "
                        + userAccess
        );
    }
}
