import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

 class RefreshExpiredAccessToken {

    static Map<String, String> refreshTokens = new HashMap<>();


    public static String refreshToken(String refreshToken) {

        if (refreshTokens.containsKey(refreshToken)) {

            String newAccessToken = UUID.randomUUID().toString();

            return newAccessToken;
        }

        return null;
    }


    public static void main(String[] args) {

        refreshTokens.put(
                "oldRefreshToken",
                "user1"
        );

        String token = refreshToken(
                "oldRefreshToken"
        );

        System.out.println(
                "New Access Token: " + token
        );
    }
}