/*
Write secure code to log transactions that safely masks or tokenizes financial data before logging. 
*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

public class SecureTransactionLogger {

    private static final Logger logger = LoggerFactory.getLogger(SecureTransactionLogger.class);
    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d+");

    /**
     * Logs a transaction safely: no PAN, no CVV, no expiry — only a masked
     * card reference and a tokenized identifier that can be looked up
     * securely elsewhere if ever needed (e.g. by fraud/support teams with
     * proper authorization).
     */
    public void logTransaction(String cardNumber, String cardHolderName, double amount) {
        String maskedCard = maskCardNumber(cardNumber);
        String token = tokenize(cardNumber);

        // Never include CVV or expiry in any log — they must never persist
        // anywhere after authorization, per PCI-DSS 3.2.

        logger.info("Transaction processed | card={} | token={} | holder={} | amount={}",
                maskedCard, token, maskCardHolderName(cardHolderName), formatAmount(amount));
    }

    /**
     * Masks a card number, preserving only the last 4 digits.
     * e.g. 4111111111111111 -> ************1111
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || !DIGITS_ONLY.matcher(cardNumber).matches() || cardNumber.length() < 4) {
            return "[invalid-card]";
        }
        int visibleDigits = 4;
        String masked = "*".repeat(cardNumber.length() - visibleDigits);
        String last4 = cardNumber.substring(cardNumber.length() - visibleDigits);
        return masked + last4;
    }

    /**
     * Masks a cardholder name so logs don't tie transactions directly to a
     * full legal name (defense-in-depth / PII minimization).
     * e.g. "Jane Doe" -> "J*** D**"
     */
    private String maskCardHolderName(String name) {
        if (name == null || name.isBlank()) return "[unknown]";
        String[] parts = name.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            result.append(part.charAt(0))
                  .append("*".repeat(Math.max(part.length() - 1, 0)))
                  .append(" ");
        }
        return result.toString().trim();
    }

    /**
     * Produces a one-way, non-reversible token derived from the card number
     * for correlating transactions in logs WITHOUT storing the PAN itself.
     *
     * In production, prefer a dedicated tokenization service / payment
     * processor's token (e.g. Stripe's payment_intent ID) over a local hash,
     * since a salted hash of a PAN can still be brute-forced given the
     * limited keyspace of card numbers. This is shown as a fallback example.
     */
    private String tokenize(String cardNumber) {
        try {
            String salted = cardNumber + getPepper(); // secret pepper, not derivable from the log
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(salted.getBytes("UTF-8"));
            return "tok_" + Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            // Fail safe: never fall back to logging the raw card number
            logger.warn("Tokenization failed, using random correlation id");
            return "tok_" + UUID.randomUUID();
        }
    }

    /**
     * Loads a secret "pepper" from a secure secrets manager / environment
     * variable — never hardcoded, never logged.
     */
    private String getPepper() {
        String pepper = System.getenv("LOG_TOKEN_PEPPER");
        if (pepper == null || pepper.isBlank()) {
            throw new IllegalStateException("LOG_TOKEN_PEPPER is not configured");
        }
        return pepper;
    }

    private String formatAmount(double amount) {
        return String.format("$%.2f", amount);
    }

    public static void main(String[] args) {
        SecureTransactionLogger logger = new SecureTransactionLogger();
        logger.logTransaction("4111111111111111", "Jane Doe", 249.99);
        // Output: Transaction processed | card=************1111 | token=tok_xxxxxxxxxxxxxxxx | holder=J*** D** | amount=$249.99
    }
}