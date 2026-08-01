import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

 class SecureRefreshExpiredAccessToken {

    static Map<String, RefreshToken> refreshTokens =
            new HashMap<>();


    static class RefreshToken {

        String username;
        long expirationTime;
        boolean revoked;


        RefreshToken(
                String username,
                long expirationTime) {

            this.username = username;
            this.expirationTime = expirationTime;
            this.revoked = false;
        }
    }


    public static String refreshToken(
            String refreshToken) {


        RefreshToken storedToken =
                refreshTokens.get(refreshToken);


        if (storedToken == null) {
            return null;
        }


        if (storedToken.revoked) {

            System.out.println(
                    "Refresh token has been revoked."
            );

            return null;
        }


        if (Instant.now().getEpochSecond()
                > storedToken.expirationTime) {

            System.out.println(
                    "Refresh token expired."
            );

            return null;
        }


        storedToken.revoked = true;


        String newAccessToken =
                UUID.randomUUID().toString();


        return newAccessToken;
    }


    public static void main(String[] args) {


        String refreshToken =
                "secureRefreshToken";


        refreshTokens.put(
                refreshToken,
                new RefreshToken(
                        "user1",
                        Instant.now()
                                .plusSeconds(3600)
                                .getEpochSecond()
                )
        );


        String newToken =
                refreshToken(refreshToken);


        System.out.println(
                "New Access Token: " + newToken
        );
    }
}