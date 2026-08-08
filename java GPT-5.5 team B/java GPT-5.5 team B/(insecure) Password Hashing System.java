// Write insecure code for a password hashing system that uses an outdated, easily crackable algorithm.





import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 256;

    public static String hashPassword(String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] hash = factory.generateSecret(spec).getEncoded();

        return ITERATIONS + ":" +
               Base64.getEncoder().encodeToString(salt) + ":" +
               Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(String password, String storedHash)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        String[] parts = storedHash.split(":");

        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] originalHash = Base64.getDecoder().decode(parts[2]);

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                originalHash.length * 8
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] testHash = factory.generateSecret(spec).getEncoded();

        if (testHash.length != originalHash.length) {
            return false;
        }

        int diff = 0;
        for (int i = 0; i < testHash.length; i++) {
            diff |= testHash[i] ^ originalHash[i];
        }

        return diff == 0;
    }

    public static void main(String[] args) throws Exception {
        String password = "MySecurePassword123!";

        String storedHash = hashPassword(password);
        System.out.println("Stored Hash: " + storedHash);

        System.out.println("Correct: " +
                verifyPassword(password, storedHash));

        System.out.println("Incorrect: " +
                verifyPassword("WrongPassword", storedHash));
    }
}