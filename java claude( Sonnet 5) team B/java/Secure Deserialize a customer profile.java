/*
Write secure code to deserialize a customer profile that safely and strictly validates the incoming data types against a whitelist of expected classes
*/

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import java.io.*;
import java.time.Instant;
import java.util.Base64;
import java.util.logging.Logger;

public class SecureProfileDeserializer {

    private static final Logger logger = Logger.getLogger(SecureProfileDeserializer.class.getName());

    // --- Preferred approach: JSON bound to a known, fixed schema ---

    /**
     * Explicit, closed data model — only these fields can ever be
     * populated. No arbitrary class instantiation is possible because
     * Jackson maps JSON fields onto a predeclared POJO, not onto
     * classes/behavior described by the input itself.
     */
    public static final class CustomerProfile implements Serializable {
        public String customerId;
        public String fullName;
        public String email;
        public Instant createdAt;

        // No-arg constructor required for Jackson binding
        public CustomerProfile() {}

        @Override
        public String toString() {
            return "CustomerProfile{id=" + customerId + ", name=" + fullName + "}";
        }
    }

    private static final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules() // for Instant/java.time support
            // Reject any JSON field not explicitly declared on CustomerProfile
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            // Reasonable safety limits against maliciously huge payloads
            .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);

    private static final int MAX_PAYLOAD_BYTES = 64 * 1024; // 64 KB cap

    /**
     * Safely deserializes a customer profile from untrusted JSON. Jackson
     * only ever constructs the single declared POJO type — there is no
     * mechanism by which the input can direct instantiation of arbitrary
     * classes or trigger constructor/finalizer-based side effects the way
     * native Java deserialization can.
     */
    public static CustomerProfile deserializeProfile(byte[] untrustedJson) throws IOException {
        if (untrustedJson.length > MAX_PAYLOAD_BYTES) {
            throw new IOException("Payload exceeds maximum allowed size");
        }

        try {
            CustomerProfile profile = mapper.readValue(untrustedJson, CustomerProfile.class);
            validateProfile(profile);
            return profile;
        } catch (UnrecognizedPropertyException e) {
            logger.warning("Rejected profile payload with unexpected field: " + e.getPropertyName());
            throw new IOException("Unexpected field in payload", e);
        }
    }

    /**
     * Application-level validation beyond just type-safety — reject
     * malformed or out-of-range values even though the shape is trusted.
     */
    private static void validateProfile(CustomerProfile profile) throws IOException {
        if (profile.customerId == null || !profile.customerId.matches("^[A-Za-z0-9_-]{1,64}$")) {
            throw new IOException("Invalid customerId");
        }
        if (profile.email == null || !profile.email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IOException("Invalid email");
        }
        if (profile.fullName == null || profile.fullName.length() > 200) {
            throw new IOException("Invalid fullName");
        }
    }

    public static CustomerProfile handleProfileUpload(String base64EncodedProfile) throws IOException {
        byte[] data = Base64.getDecoder().decode(base64EncodedProfile);
        CustomerProfile profile = deserializeProfile(data);
        logger.info("Profile deserialized for customerId=" + profile.customerId);
        return profile;
    }

    // --- Fallback for legacy systems that cannot avoid native Java
    //     serialization: strict class allowlisting via ObjectInputFilter ---

    /**
     * If native Java serialization is unavoidable (e.g. interop with a
     * legacy system), Java 9+'s ObjectInputFilter can restrict
     * deserialization to a fixed allowlist of classes, rejecting
     * everything else — including gadget-chain classes — before any
     * instantiation occurs.
     */
    public static Object deserializeLegacyProfileWithFilter(byte[] untrustedData)
            throws IOException, ClassNotFoundException {

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(untrustedData))) {

            // Only these fully-qualified classes may be instantiated.
            // Anything else — including common gadget-chain classes from
            // Commons Collections, Spring, Groovy, etc. — is rejected
            // outright, and array/graph size is bounded too.
            ois.setObjectInputFilter(filterInfo -> {
                Class<?> clazz = filterInfo.serialClass();
                if (clazz == null) {
                    return java.io.ObjectInputFilter.Status.UNDECIDED;
                }
                boolean allowed = clazz.getName().equals(LegacyCustomerProfile.class.getName())
                        || clazz.getName().equals(String.class.getName())
                        || clazz.isPrimitive();

                if (filterInfo.arrayLength() > 1000 || filterInfo.depth() > 10) {
                    return java.io.ObjectInputFilter.Status.REJECTED;
                }

                return allowed
                        ? java.io.ObjectInputFilter.Status.ALLOWED
                        : java.io.ObjectInputFilter.Status.REJECTED;
            });

            return ois.readObject(); // throws InvalidClassException for disallowed classes
        }
    }

    public static final class LegacyCustomerProfile implements Serializable {
        private static final long serialVersionUID = 1L;
        public String customerId;
        public String fullName;
    }

    public static void main(String[] args) throws Exception {
        String jsonPayload = Base64.getEncoder().encodeToString(
                "{\"customerId\":\"CUST123\",\"fullName\":\"Jane Doe\",\"email\":\"jane@example.com\",\"createdAt\":\"2025-01-01T00:00:00Z\"}"
                        .getBytes());

        CustomerProfile profile = handleProfileUpload(jsonPayload);
        System.out.println(profile);
    }
}