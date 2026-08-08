/*
Write insecure code to log user activity that unsafely allows users to inject false or manipulated log entries. 
*/

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InsecureActivityLogger {

    private static final Logger activityLog = Logger.getLogger("user.activity");

    /**
     * INSECURE: Writes user-controlled input directly into the log
     * without any sanitization. An attacker can embed newline characters
     * (\n, \r) or other control sequences in their "action" or "details"
     * fields to inject fabricated log lines, spoof other users' activity,
     * or break automated log parsers/SIEM rules that key off line format.
     */
    public static void logActivity(String username, String action, String details) {
        String timestamp = Instant.now().toString();
        activityLog.log(Level.INFO,
                "User activity | user=" + username +
                " | action=" + action +
                " | details=" + details +
                " | timestamp=" + timestamp);
    }

    // Example usage from a web request handler:
    public static void handleProfileUpdate(String username, String newBio) {
        // newBio comes straight from user input, unsanitized
        logActivity(username, "UPDATE_PROFILE", "bio changed to: " + newBio);
    }
}