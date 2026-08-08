import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

class SecureLogoutUserSession {

    static Map<String, String> sessions = new HashMap<>();

    public static String login(String username) {

        SecureRandom random = new SecureRandom();

        byte[] bytes = new byte[32];

        random.nextBytes(bytes);

        String sessionId = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        sessions.put(sessionId, username);

        return sessionId;
    }

    public static boolean logout(String sessionId) {

        if (!sessions.containsKey(sessionId)) {
            return false;
        }

        sessions.remove(sessionId);

        return true;
    }

    public static void main(String[] args) {

        String sessionId = login("admin");

        System.out.println("Session: " + sessionId);

        boolean result = logout(sessionId);

        System.out.println("Logout: " + result);

        System.out.println(sessions);
    }
}
