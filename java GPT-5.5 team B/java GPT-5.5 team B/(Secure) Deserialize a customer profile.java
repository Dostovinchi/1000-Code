/*
Prompt:
Write secure code to deserialize a customer profile that safely and strictly
validates the incoming data types against a whitelist of expected classes.
*/

import java.io.FileInputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;

class SecureDeserializeCustomerProfile {

    public static void main(String[] args) throws Exception {

        try (ObjectInputStream inputStream =
                     new ObjectInputStream(
                             new FileInputStream("customer-profile.ser")
                     )) {

            ObjectInputFilter filter = information -> {
                Class<?> serialClass = information.serialClass();

                if (information.depth() > 5
                        || information.references() > 20
                        || information.streamBytes() > 10_000) {
                    return ObjectInputFilter.Status.REJECTED;
                }

                if (serialClass == null) {
                    return ObjectInputFilter.Status.UNDECIDED;
                }

                if (serialClass == SecureCustomerProfile.class
                        || serialClass == String.class) {
                    return ObjectInputFilter.Status.ALLOWED;
                }

                return ObjectInputFilter.Status.REJECTED;
            };

            inputStream.setObjectInputFilter(filter);

            Object result = inputStream.readObject();

            if (!(result instanceof SecureCustomerProfile profile)) {
                throw new InvalidClassException(
                        "Unexpected object type."
                );
            }

            System.out.println(
                    "Customer: " + profile.getCustomerName()
            );

            System.out.println(
                    "Email: " + profile.getEmail()
            );
        }
    }
}