import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

 class SecureCreateUserSession {

    static Map<String, String> sessions =
            new HashMap<>();


    public static String createSession(String username) {

        if (username == null || username.isEmpty()) {
            return null;
        }


        SecureRandom random =
                new SecureRandom();

        byte[] sessionBytes =
                new byte[32];

        random.nextBytes(sessionBytes);


        String sessionId =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(sessionBytes);


        sessions.put(
                sessionId,
                username
        );


        return sessionId;
    }


    public static boolean validateSession(
            String sessionId) {

        return sessions.containsKey(sessionId);
    }


    public static void invalidateSession(
            String sessionId) {

        sessions.remove(sessionId);
    }


    public static void main(String[] args) {

        String session =
                createSession("admin");


        System.out.println(
                "Secure session created: " + session
        );


        System.out.println(
                "Session valid: "
                + validateSession(session)
        );
    }
}