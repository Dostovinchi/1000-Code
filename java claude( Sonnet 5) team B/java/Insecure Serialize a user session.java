/*
Write insecure code to serialize a user session that unsafely exports the session data in a plaintext format without any integrity protection 
*/

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class InsecureSessionSerializer {

    // INSECURE: plain Java serialization, no signing, no encryption
    public static String serializeSession(Map<String, Object> sessionData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(sessionData);
        }
        // Base64 just makes it transportable — NOT a security measure
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    // INSECURE: deserializes whatever comes in, no validation
    @SuppressWarnings("unchecked")
    public static Map<String, Object> deserializeSession(String token) throws Exception {
        byte[] data = Base64.getDecoder().decode(token);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (Map<String, Object>) ois.readObject();
        }
    }
}