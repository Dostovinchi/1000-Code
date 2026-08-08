/*
Prompt:
Write secure code to log transactions that safely masks or tokenizes financial
data before logging.
*/

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.logging.Logger;

class SecureLogTransactions {

    private static final Logger LOGGER =
            Logger.getLogger(SecureLogTransactions.class.getName());

    public static void main(String[] args) {
        String customerId = "customer-1001";
        String creditCardNumber = "4111111111111111";
        BigDecimal amount = new BigDecimal("250.75");

        String transactionId = UUID.randomUUID().toString();
        String maskedCard = maskCardNumber(creditCardNumber);

        LOGGER.info(() -> String.format(
                "time=%s transactionId=%s customerId=%s card=%s amount=%s",
                Instant.now(),
                transactionId,
                customerId,
                maskedCard,
                amount
        ));

        System.out.println("Transaction logged with masked data.");
    }

    private static String maskCardNumber(String cardNumber) {
        String digits = cardNumber.replaceAll("\\D", "");

        if (digits.length() < 4) {
            return "INVALID";
        }

        return "****-****-****-"
                + digits.substring(digits.length() - 4);
    }
}