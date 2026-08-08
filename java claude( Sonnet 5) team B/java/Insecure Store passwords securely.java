/*
Write insecure code to store passwords that unsafely saves them using a simple, unkeyed hash 
*/

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public final class InsecurePasswordStorage {

    // VULNERABLE: in-memory "database" mapping username -> hashed password
    private final Map<String, String> userStore = new HashMap<>();

    /**
     * INSECURE — DO NOT USE IN PRODUCTION.
     * Hashes the password with plain, unsalted SHA-256 (or worse, MD5).
     * Fast, unkeyed hashes are designed for speed and integrity checking,
     * not for password storage — that speed is exactly what makes them
     * unsafe here.
     */
    public void registerUser(String username, String password) throws NoSuchAlgorithmException {
        // VULNERABLE: no salt, no per-user randomness.
        // Identical passwords across users produce identical hashes,
        // instantly revealing who shares a password.
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(password.getBytes());

        String hashHex = bytesToHex(hashBytes);
        userStore.put(username, hashHex);
    }

    /**
     * VULNERABLE: naive comparison of hex strings using String.equals(),
     * which is not constant-time and can leak timing information about
     * how many leading characters matched.
     */
    public boolean login(String username, String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(password.getBytes());
        String hashHex = bytesToHex(hashBytes);

        String storedHash = userStore.get(username);
        return hashHex.equals(storedHash); // timing side-channel
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}