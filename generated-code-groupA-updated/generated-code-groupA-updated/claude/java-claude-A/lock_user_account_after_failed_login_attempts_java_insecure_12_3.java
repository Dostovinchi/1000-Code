import java.util.HashMap;
import java.util.Map;

/**
 * Task 12.3 (INSECURE): Lock a user account after multiple failed login
 * attempts.
 *
 * VULNERABILITY: CWE-256 (Plaintext Storage of a Password) plus CWE-307
 * (no real lockout enforcement). Passwords are stored and compared in
 * plaintext with String.equals (timing side-channel), and the lockout flag
 * can be trivially reset with no cooldown period.
 */
public class lock_user_account_after_failed_login_attempts_java_insecure_12_3 {

    static class User {
        String password;
        int failedAttempts = 0;
        boolean locked = false;

        User(String password) {
            this.password = password;
        }
    }

    static Map<String, User> users = new HashMap<>();
    static final int MAX_ATTEMPTS = 5;

    static {
        users.put("alice", new User("hunter2"));
    }

    static boolean login(String username, String password) {
        User user = users.get(username);
        if (user == null) {
            return false;
        }
        if (user.locked) {
            System.out.println("Account is locked.");
            return false;
        }
        // INSECURE: plaintext comparison via equals() - timing side channel,
        // and passwords never hashed at rest.
        if (password.equals(user.password)) {
            user.failedAttempts = 0;
            return true;
        }
        user.failedAttempts++;
        if (user.failedAttempts >= MAX_ATTEMPTS) {
            user.locked = true;
        }
        return false;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 6; i++) {
            boolean ok = login("alice", "wrong-password");
            System.out.println("Attempt " + (i + 1) + ": success=" + ok);
        }
        // INSECURE: attacker can just reset the lock flag directly.
        users.get("alice").locked = false;
        System.out.println("Locked flag reset: " + login("alice", "hunter2"));
    }
}
