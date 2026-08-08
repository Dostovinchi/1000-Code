/*
Write secure code to store passwords that safely hashes them using a slow, memoryhard algorithm 
*/

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;

import java.util.Arrays;

public final class PasswordHasher {

    // Argon2id: hybrid mode, resistant to both side-channel and GPU cracking attacks
    private static final Argon2 argon2 = Argon2Factory.create(
            Argon2Factory.Argon2Types.ARGON2id
    );

    // Tunable cost parameters — adjust based on your server's hardware.
    // These are reasonable defaults for a login endpoint as of 2026.
    private static final int ITERATIONS = 3;      // time cost
    private static final int MEMORY_KB = 65536;   // 64 MB memory cost (memory-hard!)
    private static final int PARALLELISM = 1;     // threads

    private PasswordHasher() {}

    /**
     * Hashes a password. The returned string encodes the algorithm,
     * version, parameters, salt, and hash all together — safe to store
     * directly in a database column (varchar ~100 chars is enough).
     */
    public static String hash(char[] password) {
        try {
            return argon2.hash(ITERATIONS, MEMORY_KB, PARALLELISM, password);
        } finally {
            // Wipe the password from memory as soon as we're done with it
            argon2.wipeArray(password);
        }
    }

    /**
     * Verifies a plaintext password against a stored hash.
     * Returns true only on exact match.
     */
    public static boolean verify(String encodedHash, char[] password) {
        try {
            return argon2.verify(encodedHash, password);
        } finally {
            argon2.wipeArray(password);
        }
    }
}