import java.util.HashMap;
import java.util.Map;

class LogoutUserSession {

    static Map<String, Boolean> sessions = new HashMap<>();

    public static void login(String username) {
        sessions.put(username, true);
    }

    public static void logout(String username) {

        if (sessions.containsKey(username)) {
            sessions.put(username, false);
        }

        System.out.println("User logged out.");
    }

    public static void main(String[] args) {

        login("admin");

        logout("admin");

        System.out.println(sessions);
    }
}
