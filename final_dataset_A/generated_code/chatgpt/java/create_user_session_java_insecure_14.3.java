import java.util.HashMap;
import java.util.Map;

 class CreateUserSession {

    static Map<String, String> sessions = new HashMap<>();


    public static String createSession(String username) {

        String sessionId = username + "_session";

        sessions.put(
                sessionId,
                username
        );

        return sessionId;
    }


    public static void main(String[] args) {

        String session =
                createSession("admin");

        System.out.println(
                "Session created: " + session
        );
    }
}