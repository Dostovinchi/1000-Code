import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

 class SecureReauthenticateUserBeforePasswordChange {

    static Map<String, User> users =
            new HashMap<>();


    static class User {

        String passwordHash;
        int failedAttempts;

        User(String passwordHash) {
            this.passwordHash = passwordHash;
            this.failedAttempts = 0;
        }
    }


    public static String hashPassword(
            String password) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );


            byte[] hash =
                    digest.digest(
                            password.getBytes()
                    );


            StringBuilder result =
                    new StringBuilder();


            for (byte b : hash) {

                result.append(
                        String.format(
                                "%02x",
                                b
                        )
                );
            }


            return result.toString();


        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException();
        }
    }


    public static boolean authenticate(
            String username,
            String password) {


        User user =
                users.get(username);


        if (user == null) {
            return false;
        }


        String passwordHash =
                hashPassword(password);


        if (passwordHash.equals(
                user.passwordHash)) {

            user.failedAttempts = 0;
            return true;

        } else {

            user.failedAttempts++;
            return false;
        }
    }


    public static boolean changePassword(
            String username,
            String currentPassword,
            String newPassword) {


        if (!authenticate(
                username,
                currentPassword)) {

            return false;
        }


        if (newPassword == null
                || newPassword.length() < 8) {

            return false;
        }


        users.get(username).passwordHash =
                hashPassword(newPassword);


        return true;
    }


    public static void main(String[] args) {


        users.put(
                "admin",
                new User(
                        hashPassword(
                                "OldPassword123"
                        )
                )
        );


        boolean result =
                changePassword(
                        "admin",
                        "OldPassword123",
                        "NewPassword456"
                );


        System.out.println(
                "Password changed: "
                        + result
        );
    }
}