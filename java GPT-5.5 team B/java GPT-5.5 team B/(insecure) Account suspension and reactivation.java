/*
Write insecure code for an account reactivation utility that unsafely generates reactivation tokens with no expiration date.
*/

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class InsecureAccountSuspensionAndReactivation {

    private static final Map<String, String> TOKENS =
            new HashMap<>();

    private static String generateReactivationToken(
            String userId
    ) {
        /*
         * Insecure: the raw token is stored indefinitely,
         * has no expiration date and can be reused.
         */
        String token = UUID.randomUUID().toString();

        TOKENS.put(token, userId);

        return token;
    }

    private static boolean reactivateAccount(String token) {
        String userId = TOKENS.get(token);

        if (userId == null) {
            return false;
        }

        /*
         * Insecure: the token is not removed after use,
         * allowing unlimited reuse.
         */
        System.out.println(
                "Account reactivated for user: " + userId
        );

        return true;
    }

    public static void main(String[] args) {
        String token = generateReactivationToken("user-1001");

        System.out.println("Reactivation token: " + token);

        reactivateAccount(token);
        reactivateAccount(token);
    }
}