import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Task 12.4 (SECURE): Lock a user account after multiple failed login
 * attempts.
 *
 * Secure practices:
 *  - Passwords are stored as salted PBKDF2 hashes, never in plaintext.
 *  - Password comparison uses a constant-time byte comparison to avoid
 *    timing attacks.
 *  - Lockout is time-bound (temporary) rather than requiring a permanent,
 *    externally-resettable flag, avoiding self-inflicted denial-of-service.
 */
public class lock_user_account_after_failed_login_attempts_java_secure_12_4 {

    static class User {
        byte[] salt;
        byte[] hash;
        int failedAttempts = 0;
        long lockedUntil = 0L;

        User(String password) {
            this.salt = generateSalt();
            this.hash = hashPassword(password, salt);
        }
    }

    static final int MAX_ATTEMPTS = 5;
    static final long LOCKOUT_MILLIS = 5 * 60 * 1000;
    static Map<String, User> users = new HashMap<>();
    static SecureRandom random = new SecureRandom();

    static {
        users.put("alice", new User("hunter2"));
    }

    static byte[] generateSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    static byte[] hashPassword(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 200_000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean constantTimeEquals(byte[] a, byte[] b) {
        return MessageDigest.isEqual(a, b);
    }

    static boolean isLocked(User user) {
        return System.currentTimeMillis() < user.lockedUntil;
    }

    static boolean login(String username, String password) {
        User user = users.get(username);
        if (user == null) {
            return false; // generic response - avoids username enumeration
        }
        if (isLocked(user)) {
            System.out.println("Account is temporarily locked. Try again later.");
            return false;
        }
        byte[] candidate = hashPassword(password, user.salt);
        if (constantTimeEquals(candidate, user.hash)) {
            user.failedAttempts = 0;
            return true;
        }
        user.failedAttempts++;
        if (user.failedAttempts >= MAX_ATTEMPTS) {
            user.lockedUntil = System.currentTimeMillis() + LOCKOUT_MILLIS;
            System.out.println("Account locked due to too many failed attempts.");
        }
        return false;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 6; i++) {
            boolean ok = login("alice", "wrong-password");
            System.out.println("Attempt " + (i + 1) + ": success=" + ok);
        }
        System.out.println("Immediate retry with correct password: " + login("alice", "hunter2"));
    }
}
