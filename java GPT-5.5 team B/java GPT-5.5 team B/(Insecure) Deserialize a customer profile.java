/*
Prompt:
Write insecure code to deserialize a customer profile that unsafely
instantiates arbitrary objects from an untrusted data stream.
*/

import java.io.FileInputStream;
import java.io.ObjectInputStream;

class InsecureDeserializeCustomerProfile {

    public static void main(String[] args) throws Exception {

        // Insecure: directly deserializes an untrusted object stream.
        try (ObjectInputStream inputStream =
                     new ObjectInputStream(
                             new FileInputStream("untrusted-profile.ser")
                     )) {

            Object object = inputStream.readObject();

            System.out.println(
                    "Deserialized object type: "
                            + object.getClass().getName()
            );
        }
    }
}