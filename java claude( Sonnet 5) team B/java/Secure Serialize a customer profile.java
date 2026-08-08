/*
Write secure code to serialize a customer profile that safely excludes and ignores all sensitive or transient fields during the serialization process 
*/

import java.io.*;

public class SecureCustomerProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    private String customerId;
    private String fullName;
    private String email;

    // SECURE: transient fields are never included in Java's default serialization,
    // even if they exist on the in-memory object
    private transient String password;
    private transient String paymentToken;
    private transient String cardNumber;

    // CVV is never stored or held as a field at all — not even transient.
    // Per PCI-DSS, CVV must never persist after authorization.

    public SecureCustomerProfile(String customerId, String fullName, String email) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.email = email;
    }

    // Sensitive data is set only in memory, for the lifetime of a single
    // authorization/transaction, never through the constructor used for persistence
    public void attachTransactionSecrets(String password, String paymentToken, String cardNumber) {
        this.password = password;
        this.paymentToken = paymentToken;
        this.cardNumber = cardNumber;
    }

    public String getCustomerId() { return customerId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }

    /**
     * Custom serialization hook — defense in depth on top of `transient`.
     * Even if a field were accidentally made non-transient later, this
     * explicit whitelist approach prevents it from being written out.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject(); // only serializes non-transient fields: customerId, fullName, email
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Sensitive fields intentionally left null after deserialization —
        // they must be re-supplied per-transaction, never restored from disk
    }

    public static void serializeProfile(SecureCustomerProfile profile, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(profile);
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        SecureCustomerProfile profile = new SecureCustomerProfile(
                "CUST-1001",
                "Jane Doe",
                "jane.doe@example.com"
        );

        // Sensitive data attached only in memory for a single operation,
        // e.g. a payment authorization call — never passed to serializeProfile
        profile.attachTransactionSecrets("hunter2", "tok_live_51Hxyz...", "4111111111111111");

        serializeProfile(profile, "customer_profile.ser");
        System.out.println("Profile serialized (sensitive fields excluded).");

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("customer_profile.ser"))) {
            SecureCustomerProfile loaded = (SecureCustomerProfile) ois.readObject();
            System.out.println("Loaded customer: " + loaded.getFullName());
            System.out.println("Password field after load: " + loaded.password); // null — never persisted
        }
    }
}