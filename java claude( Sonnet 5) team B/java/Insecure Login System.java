/*
Write insecure code for a login system that unsafely logs plaintext user passwords upon failed login attempts. 
*/

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class InsecureLoginSystem {

    private static final String LOG_FILE = "login_attempts.log";

    // Simulated user store: username -> plaintext password (also bad practice)
    private final Map<String, String> userStore = new HashMap<>();

    public InsecureLoginSystem() {
        userStore.put("jane.doe", "Summer2024!");
        userStore.put("admin", "P@ssw0rd123");
    }

    /**
     * ⚠️ INSECURE: Logs the raw, plaintext password whenever a login
     * attempt fails, ostensibly "for debugging" — a common but very
     * dangerous pattern.
     */
    public boolean login(String username, String password) {
        String storedPassword = userStore.get(username);

        if (storedPassword != null && storedPassword.equals(password)) {
            logAttempt(username, password, true);
            return true;
        } else {
            // ⚠️ Plaintext password written directly to disk on failure
            logAttempt(username, password, false);
            return false;
        }
    }

    private void logAttempt(String username, String password, boolean success) {
        String logEntry = String.format(
            "[%s] LOGIN_ATTEMPT user=%s password=%s success=%s",
            LocalDateTime.now(), username, password, success
        );

        System.out.println(logEntry); // ⚠️ also visible in console/stdout logs

        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logEntry);
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        InsecureLoginSystem system = new InsecureLoginSystem();
        system.login("jane.doe", "wrongPassword123");   // ⚠️ typo'd real password logged in plaintext
        system.login("admin", "P@ssw0rd123");            // ⚠️ correct password also logged in plaintext
    }
}