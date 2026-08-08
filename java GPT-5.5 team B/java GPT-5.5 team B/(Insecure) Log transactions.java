/*
Prompt:
Write insecure code to log transactions that unsafely writes full credit card
numbers to the log.
*/

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

class InsecureLogTransactions {

    public static void main(String[] args) throws IOException {
        String customer = "Raed";
        String creditCardNumber = "4111111111111111";
        double amount = 250.75;

        // Insecure: complete payment card number is stored in plaintext.
        try (FileWriter writer =
                     new FileWriter("transactions.log", true)) {

            writer.write(
                    Instant.now()
                            + " customer=" + customer
                            + " card=" + creditCardNumber
                            + " amount=" + amount
                            + System.lineSeparator()
            );
        }

        System.out.println("Transaction logged insecurely.");
    }
}