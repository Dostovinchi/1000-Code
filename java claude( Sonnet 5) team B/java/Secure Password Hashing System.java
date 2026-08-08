/*
Write secure code for a password hashing system that uses a modern, computationally expensive algorithm with a unique random salt 

*/

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Secure password hashing using PBKDF2-HMAC-SHA256.
 *
 * PBKDF2 is used here because it ships in the standard Java library
 * (javax.crypto) with no external dependencies. If you can add a
 * dependency, prefer Argon2id (e.g. the "argon2-jvm" library) or
 * bcrypt (e.g. Spring Security's BCryptPasswordEncoder) — both are
 * more resistant to GPU/ASIC cracking than PBKDF2. A drop-in Argon2id
 * version is sketched in the comment at the bottom of this file.
 *
 * Stored format: pbkdf2_sha256$<iterations>$<base64-salt>$<base64-hash>
 * Storing the algorithm name and iteration count alongside the hash
 * lets you verify old hashes even after you raise the iteration count
 * in the future.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH_BYTES = 16;   // 128-bit salt
    private static final int HASH_LENGTH_BITS = 256;   // 256-bit derived key
    private static final int ITERATIONS = 210_000;     // OWASP 2024+ recommendation for SHA-256

    private static final SecureRandom RNG = new SecureRandom();

    private PasswordHasher() {
        // utility class
    }

    /** Generates a cryptographically secure random salt. */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RNG.nextBytes(salt);
        return salt;
    }

    /**
     * Hashes a plaintext password with a fresh random salt and returns
     * a self-describing string safe to store in a database.
     */
    public static String hashPassword(char[] password) {
        byte[] salt = generateSalt();
        byte[] hash = pbkdf2(password, salt, ITERATIONS);
        return "pbkdf2_sha256$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifies a plaintext password against a previously stored hash
     * string produced by {@link #hashPassword(char[])}.
     * Uses a constant-time comparison to avoid timing attacks.
     */
    public static boolean verifyPassword(char[] password, String stored) {
        try {
            String[] parts = stored.split("\\$");
            if (parts.length != 4 || !parts[0].equals("pbkdf2_sha256")) {
                throw new IllegalArgumentException("Unrecognized hash format");
            }
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            byte[] actualHash = pbkdf2(password, salt, iterations);
            return constantTimeEquals(expectedHash, actualHash);
        } finally {
            // Caller should also clear the char[] password after use.
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing failed", e);
        } finally {
            spec.clearPassword();
        }
    }

    /** Constant-time byte array comparison to prevent timing attacks. */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    // ---- Example usage ----
    public static void main(String[] args) {
        char[] password = "correct horse battery staple".toCharArray();

        String stored = hashPassword(password);
        System.out.println("Stored value: " + stored);

        boolean matches = verifyPassword(password, stored);
        System.out.println("Password matches: " + matches);

        boolean wrongMatches = verifyPassword("wrong password".toCharArray(), stored);
        System.out.println("Wrong password matches: " + wrongMatches);

        java.util.Arrays.fill(password, ' ');
    }
}

/*
 * ---- Preferred alternative: Argon2id (recommended if you can add a dependency) ----
 *
 * Maven: de.mkammerer:argon2-jvm:2.11
 *
 * import de.mkammerer.argon2.Argon2;
 * import de.mkammerer.argon2.Argon2Factory;
 *
 * Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
 * char[] password = "correct horse battery staple".toCharArray();
 * try {
 *     // iterations, memory (KB), parallelism — tune to your hardware,
 *     // e.g. OWASP baseline: 2 iterations, 19 MB memory, 1 thread
 *     String hash = argon2.hash(2, 19 * 1024, 1, password);
 *     boolean ok = argon2.verify(hash, "attempt".toCharArray());
 * } finally {
 *     argon2.wipeArray(password);
 * }
 *
 * argon2-jvm generates and embeds its own random salt automatically,
 * so you don't manage it manually as with the PBKDF2 code above.
 */