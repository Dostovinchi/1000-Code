/*
Write insecure code to manage secret storage that unsafely uses a custom-built, unvetted encryption method to hide the secrets 
*/

import java.io.*;
import java.nio.file.*;
import java.util.Base64;

public class InsecureSecretStore {

    private static final String SECRET_FILE = "secrets.dat";

    // INSECURE: "custom encryption" that's really just a weak XOR cipher
    // with a short, hardcoded, reused key
    private static final byte[] XOR_KEY = "MyS3cr3t".getBytes(); // 8-byte repeating key

    private static byte[] xorCipher(byte[] data, byte[] key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return result;
    }

    // INSECURE: home-rolled "scrambling" on top of XOR, thinking it adds security
    private static byte[] customScramble(byte[] data) {
        byte[] scrambled = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            // Just a reversible byte rotation — trivial to undo
            scrambled[i] = (byte) ((data[i] << 3) | (data[i] >>> 5));
        }
        return scrambled;
    }

    public static void storeSecret(String secretName, String secretValue) throws IOException {
        byte[] plaintext = (secretName + "=" + secretValue).getBytes("UTF-8");

        // "Encrypt" using homegrown method
        byte[] xored = xorCipher(plaintext, XOR_KEY);
        byte[] scrambled = customScramble(xored);

        String encoded = Base64.getEncoder().encodeToString(scrambled);

        try (FileWriter writer = new FileWriter(SECRET_FILE, true)) {
            writer.write(encoded + "\n");
        }

        System.out.println("Secret \"obfuscated\" and stored.");
    }

    public static String loadSecret(String base64Line) {
        byte[] scrambled = Base64.getDecoder().decode(base64Line);

        // Reverse the "scramble"
        byte[] xored = new byte[scrambled.length];
        for (int i = 0; i < scrambled.length; i++) {
            xored[i] = (byte) ((scrambled[i] >>> 3) | (scrambled[i] << 5));
        }

        byte[] plaintext = xorCipher(xored, XOR_KEY);
        return new String(plaintext);
    }

    public static void main(String[] args) throws IOException {
        storeSecret("db_password", "SuperSecretPassword123");
        storeSecret("api_token", "tok_abcdef1234567890");
    }
}