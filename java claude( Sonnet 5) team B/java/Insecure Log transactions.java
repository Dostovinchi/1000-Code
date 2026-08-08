/*
Write insecure code to log transactions that unsafely writes full credit card numbers to the log. 
*/

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class TransactionLogger {

    private static final String LOG_FILE = "transactions.log";

    /**
     * ⚠️ INSECURE: Logs the full, unmasked credit card number (and CVV)
     * in plaintext to a log file.
     */
    public void logTransaction(String cardNumber, String cvv, String expiryDate,
                                String cardHolderName, double amount) {
        String logEntry = String.format(
            "[%s] Transaction processed | Card: %s | CVV: %s | Expiry: %s | Holder: %s | Amount: $%.2f",
            LocalDateTime.now(), cardNumber, cvv, expiryDate, cardHolderName, amount
        );

        System.out.println(logEntry); // ⚠️ also prints sensitive data to console/stdout logs

        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logEntry);
        } catch (IOException e) {
            // ⚠️ exception message could also leak sensitive data if it echoes input
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        TransactionLogger logger = new TransactionLogger();
        logger.logTransaction("4111111111111111", "123", "12/27", "Jane Doe", 249.99);
    }
}