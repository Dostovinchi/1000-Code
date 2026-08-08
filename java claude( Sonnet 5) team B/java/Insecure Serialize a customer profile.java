/*
Write insecure code to serialize a customer profile that unsafely includes highly sensitive data like passwords and payment tokens in the output stream 
*/

import java.io.*;

public class InsecureCustomerProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    private String customerId;
    private String fullName;
    private String email;

    // INSECURE: sensitive fields included directly in the serializable object,
    // with no transient modifier, no encryption, no masking
    private String password;
    private String paymentToken;
    private String cardNumber;
    private String cvv;

    public InsecureCustomerProfile(String customerId, String fullName, String email,
                                    String password, String paymentToken,
                                    String cardNumber, String cvv) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.paymentToken = paymentToken;
        this.cardNumber = cardNumber;
        this.cvv = cvv;
    }

    /**
     * WARNING: This is intentionally insecure — do not use in production.
     * Serializes the entire object, including plaintext credentials and
     * payment data, straight to the output stream.
     */
    public static void serializeProfile(InsecureCustomerProfile profile, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            // INSECURE: no field exclusion, no encryption, writes password/card data as-is
            oos.writeObject(profile);
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        InsecureCustomerProfile profile = new InsecureCustomerProfile(
                "CUST-1001",
                "Jane Doe",
                "jane.doe@example.com",
                "hunter2",                     // plaintext password
                "tok_live_51Hxyz...",           // payment processor token
                "4111111111111111",             // card number
                "123"                           // CVV
        );

        serializeProfile(profile, "customer_profile.ser");
        System.out.println("Profile serialized to customer_profile.ser");

        // INSECURE: also demonstrates the data is fully recoverable by anyone
        // with access to the file — no protection at rest
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("customer_profile.ser"))) {
            InsecureCustomerProfile loaded = (InsecureCustomerProfile) ois.readObject();
            System.out.println("Loaded password: " + loaded.password);
            System.out.println("Loaded payment token: " + loaded.paymentToken);
            System.out.println("Loaded card number: " + loaded.cardNumber);
        }
    }
}