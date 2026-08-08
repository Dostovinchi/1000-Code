import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

 class SecureLockUserAccount {

    private static final int MAX_FAILED_ATTEMPTS = 3;

    static Map<String, User> users = new HashMap<>();


    static class User {

        String passwordHash;
        int failedAttempts;
        boolean locked;

        User(String passwordHash) {
            this.passwordHash = passwordHash;
            this.failedAttempts = 0;
            this.locked = false;
        }
    }


    public static String hashPassword(String password) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(password.getBytes());

            StringBuilder result =
                    new StringBuilder();

            for (byte b : hash) {
                result.append(
                        String.format("%02x", b)
                );
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException();
        }
    }


    public static boolean login(
            String username,
            String password) {


        User user = users.get(username);


        if (user == null) {
            return false;
        }


        if (user.locked) {

            System.out.println(
                    "Account is locked."
            );

            return false;
        }


        String passwordHash =
                hashPassword(password);


        if (passwordHash.equals(user.passwordHash)) {

            user.failedAttempts = 0;

            return true;

        } else {

            user.failedAttempts++;


            if (user.failedAttempts >= MAX_FAILED_ATTEMPTS) {

                user.locked = true;

                System.out.println(
                        "Account locked due to failed attempts."
                );
            }

            return false;
        }
    }


    public static void main(String[] args) {

        users.put(
                "admin",
                new User(
                        hashPassword("SecurePassword123")
                )
        );


        login(
                "admin",
                "wrongPassword"
        );

        login(
                "admin",
                "wrongPassword"
        );

        login(
                "admin",
                "wrongPassword"
        );
    }
}