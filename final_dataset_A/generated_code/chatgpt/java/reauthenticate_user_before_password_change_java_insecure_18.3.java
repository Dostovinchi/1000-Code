import java.util.HashMap;
import java.util.Map;

 class ReauthenticateUserBeforePasswordChange {

    static Map<String, String> users =
            new HashMap<>();


    public static boolean changePassword(
            String username,
            String newPassword) {

        if (users.containsKey(username)) {

            users.put(
                    username,
                    newPassword
            );

            return true;
        }

        return false;
    }


    public static void main(String[] args) {

        users.put(
                "admin",
                "oldPassword"
        );


        boolean result =
                changePassword(
                        "admin",
                        "newPassword"
                );


        System.out.println(
                "Password changed: " + result
        );
    }
}