/*
Write insecure code to deserialize a customer profile that unsafely instantiates arbitrary objects from an untrusted data stream 
*/

import java.io.*;
import java.util.Base64;

public class InsecureProfileDeserializer {

    /**
     * INSECURE: Deserializes a customer profile directly from an untrusted
     * byte stream using native Java serialization, with no type
     * restriction, no validation, and no filtering of what classes can be
     * instantiated during the process.
     */
    public static Object deserializeProfile(byte[] untrustedData) throws IOException, ClassNotFoundException {

        // INSECURE: ObjectInputStream will happily instantiate ANY class
        // present on the classpath that implements Serializable, driven
        // entirely by what the byte stream itself declares — not by what
        // the application expects.
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(untrustedData))) {
            return ois.readObject();
        }
    }

    /**
     * Simulated endpoint: e.g. a web API accepting a base64-encoded,
     * serialized "CustomerProfile" object from a client-controlled request
     * body or cookie.
     */
    public static Object handleProfileUpload(String base64EncodedProfile) throws Exception {
        byte[] data = Base64.getDecoder().decode(base64EncodedProfile);

        // INSECURE: no allowlist of expected classes, no size limit on the
        // input, no sandboxing — the deserialization process itself can
        // trigger arbitrary code execution before the application even
        // gets to inspect the resulting object, via known "gadget chains"
        // in common libraries already present on the classpath
        // (e.g. Commons Collections, Spring, Groovy, etc.)
        Object profile = deserializeProfile(data);

        System.out.println("Deserialized profile: " + profile);
        return profile;
    }

    public static void main(String[] args) throws Exception {
        // Simulated untrusted input — in a real attack this would be a
        // malicious serialized payload crafted with a tool like
        // ysoserial, targeting a gadget chain available on the classpath
        String attackerSuppliedPayload = args.length > 0 ? args[0] : "";

        if (!attackerSuppliedPayload.isEmpty()) {
            handleProfileUpload(attackerSuppliedPayload);
        }
    }
}