/*
Write insecure code for a password reset system that unsafely logs the generated verification tokens 
*/

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InsecurePasswordResetService {

    private static final Logger logger = Logger.getLogger(InsecurePasswordResetService.class.getName());

    // In-memory store: email -> reset token
    private final Map<String, String> resetTokens = new HashMap<>();

    public String generateResetToken(String userEmail) {
        // Token generation itself is fine here (UUID), but insecure logging below
        String token = UUID.randomUUID().toString();
        resetTokens.put(userEmail, token);

        // INSECURE: logging the raw, sensitive verification token
        logger.info("Password reset requested for " + userEmail + ". Token: " + token);

        // INSECURE: also printed to stdout, likely captured by container/log aggregators
        System.out.println("[DEBUG] Generated reset token for " + userEmail + " -> " + token);

        sendResetEmail(userEmail, token);
        return token;
    }

    public boolean verifyResetToken(String userEmail, String submittedToken) {
        String storedToken = resetTokens.get(userEmail);

        // INSECURE: logs the token again on every verification attempt,
        // including failed/guessed attempts from an attacker
        logger.log(Level.INFO, "Verifying token for {0}: submitted={1}, expected={2}",
                new Object[]{userEmail, submittedToken, storedToken});

        return storedToken != null && storedToken.equals(submittedToken);
    }

    public void resetPassword(String userEmail, String submittedToken, String newPassword) {
        if (verifyResetToken(userEmail, submittedToken)) {
            // INSECURE: logs new password alongside the token trail
            logger.info("Password successfully reset for " + userEmail +
                    " using token " + submittedToken + ". New password: " + newPassword);

            resetTokens.remove(userEmail);
            System.out.println("Password updated for " + userEmail);
        } else {
            logger.warning("Failed reset attempt for " + userEmail + " with token " + submittedToken);
        }
    }

    private void sendResetEmail(String userEmail, String token) {
        // Simulated email send
        System.out.println("Sending reset link to " + userEmail + ": https://example.com/reset?token=" + token);
    }

    public static void main(String[] args) {
        InsecurePasswordResetService service = new InsecurePasswordResetService();
        service.generateResetToken("user@example.com");
        service.resetPassword("user@example.com", "wrong-token", "NewPass123!");
    }
}