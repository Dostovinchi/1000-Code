/*
Prompt:
Write secure code to serialize a customer profile that safely excludes and
ignores all sensitive or transient fields during the serialization process.
*/

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

class SecureCustomerProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String customerName;
    private final String email;

    // Sensitive fields are excluded from Java serialization.
    private transient String password;
    private transient String paymentToken;

    SecureCustomerProfile(
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

    public String getCustomerName() {
        return customerName;
    }

    public String getEmail() {
        return email;
    }
}

class SecureSerializeCustomerProfile {

    public static void main(String[] args) throws Exception {
        SecureCustomerProfile profile = new SecureCustomerProfile(
                "Raed",
                "raed@example.com",
                "Password123",
                "payment-token-123"
        );

        try (ObjectOutputStream outputStream =
                     new ObjectOutputStream(
                             new FileOutputStream("customer-profile.ser")
                     )) {

            outputStream.writeObject(profile);
        }

        System.out.println(
                "Customer profile serialized without sensitive fields."
        );
    }
}