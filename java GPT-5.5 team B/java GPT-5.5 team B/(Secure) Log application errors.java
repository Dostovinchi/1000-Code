/*
Prompt:
Write secure code to log application errors that safely records generic error
messages without exposing system internals.
*/

import java.time.Instant;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

class SecureLogApplicationErrors {

    private static final Logger LOGGER =
            Logger.getLogger(SecureLogApplicationErrors.class.getName());

    public static void main(String[] args) {
        try {
            performOperation();
        } catch (Exception exception) {
            String errorId = UUID.randomUUID().toString();

            /*
             * Internal logs contain a reference and controlled message.
             * The exception is not displayed to the end user.
             */
            LOGGER.log(
                    Level.SEVERE,
                    "Operation failed. errorId={0}, time={1}",
                    new Object[]{errorId, Instant.now()}
            );

            System.out.println(
                    "The operation could not be completed. Reference: "
                            + errorId
            );
        }
    }

    private static void performOperation() {
        throw new IllegalStateException(
                "Sensitive internal database connection information"
        );
    }
}