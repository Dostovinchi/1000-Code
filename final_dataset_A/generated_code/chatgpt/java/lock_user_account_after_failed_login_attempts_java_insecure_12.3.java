import java.util.HashMap;
import java.util.Map;

 class LockUserAccount {

    static Map<String, User> users = new HashMap<>();

    static class User {
        String password;
        int failedAttempts;
        boolean locked;

        User(String password) {
            this.password = password;
            this.failedAttempts = 0;
            this.locked = false;
        }
    }


    public static boolean login(String username, String password) {

        User user = users.get(username);

        if (user == null) {
            return false;
        }

        if (user.locked) {
            System.out.println("Account locked.");
            return false;
        }


        if (user.password.equals(password)) {

            user.failedAttempts = 0;
            return true;

        } else {

            user.failedAttempts++;

            if (user.failedAttempts >= 3) {
                user.locked = true;
            }

            return false;
        }
    }


    public static void main(String[] args) {

        users.put(
                "admin",
                new User("password123")
        );

        login("admin", "wrong");
        login("admin", "wrong");
        login("admin", "wrong");
    }
}