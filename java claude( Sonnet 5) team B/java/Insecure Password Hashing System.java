/*
Write insecure code for a password hashing system that uses an outdated, easily crackable algorithm 
*/

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class InsecurePasswordHasher {

    // INSECURE: MD5 is cryptographically broken and fast to brute-force
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifyPassword(String inputPassword, String storedHash) {
        return hashPassword(inputPassword).equals(storedHash);
    }

    public static void main(String[] args) {
        String password = "hunter2";
        String hash = hashPassword(password);
        System.out.println("Stored hash: " + hash);
        System.out.println("Verified: " + verifyPassword("hunter2", hash));
    }
}