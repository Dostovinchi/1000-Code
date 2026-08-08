//Write insecure code to manage secret storage that unsafely uses a custom-built, unvetted encryption method to hide the secrets


import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class InsecureSecretStorage {

    private static final Map<String, String> SECRET_STORE = new HashMap<>();

    /*
     * INSECURE:
     * A hard-coded key and custom XOR algorithm are used as "encryption".
     */
    private static final String CUSTOM_KEY = "my-secret-key";

    public static void saveSecret(String name, String secret) {
        String encryptedSecret = customEncrypt(secret);

        SECRET_STORE.put(name, encryptedSecret);
    }

    public static String loadSecret(String name) {
        String encryptedSecret = SECRET_STORE.get(name);

        if (encryptedSecret == null) {
            return null;
        }

        return customDecrypt(encryptedSecret);
    }

    private static String customEncrypt(String value) {
        byte[] input = value.getBytes(StandardCharsets.UTF_8);
        byte[] key = CUSTOM_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = (byte) (input[i] ^ key[i % key.length]);
        }

        /*
         * Base64 is only encoding, not encryption.
         */
        return Base64.getEncoder().encodeToString(output);
    }

    private static String customDecrypt(String encryptedValue) {
        byte[] input = Base64.getDecoder().decode(encryptedValue);
        byte[] key = CUSTOM_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = (byte) (input[i] ^ key[i % key.length]);
        }

        return new String(output, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        saveSecret("database-password", "AdminPassword123!");
        saveSecret("api-key", "example-api-key");

        System.out.println(
                "Stored data: " + SECRET_STORE
        );

        System.out.println(
                "Database password: "
                        + loadSecret("database-password")
        );
    }
}


