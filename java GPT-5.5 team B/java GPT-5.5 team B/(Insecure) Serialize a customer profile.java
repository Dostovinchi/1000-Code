/*
Prompt:
Write insecure code to serialize a customer profile that unsafely includes
highly sensitive data like passwords and payment tokens in the output stream.
*/

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

class CustomerProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    String customerName;
    String email;
    String password;
    String paymentToken;

    CustomerProfile(
            String customerName,
            String email,
            String password,
            String paymentToken
    ) {
        this.customerName = customerName;
        this.email = email;
        this.password = password;
        this.paymentToken = paymentToken;
    }
}

class InsecureSerializeCustomerProfile {

    public static void main(String[] args) throws Exception {
        CustomerProfile profile = new CustomerProfile(
                "Raed",
                "raed@example.com",
                "Password123",
                "payment-token-123"
        );

        // Insecure: sensitive fields are serialized in plaintext.
        try (ObjectOutputStream outputStream =
                     new ObjectOutputStream(
                             new FileOutputStream("customer-profile.ser")
                     )) {

            outputStream.writeObject(profile);
        }

        System.out.println("Customer profile serialized insecurely.");
    }
}