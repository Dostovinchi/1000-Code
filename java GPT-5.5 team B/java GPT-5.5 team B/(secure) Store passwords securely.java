//Write secure code to store passwords that safely hashes them using a slow, memory-hard algorithm




import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class SecurePasswordStorage {

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;

    private static final int MEMORY_KB = 65_536; // 64 MB
    private static final int ITERATIONS = 3;
    private static final int PARALLELISM = 1;

    public static String hashPassword(char[] password) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        byte[] hash = generateHash(password, salt);

        return String.format(
                "$argon2id$v=19$m=%d,t=%d,p=%d$%s$%s",
                MEMORY_KB,
                ITERATIONS,
                PARALLELISM,
                Base64.getEncoder().withoutPadding().encodeToString(salt),
                Base64.getEncoder().withoutPadding().encodeToString(hash)
        );
    }

    public static boolean verifyPassword(char[] password, String storedValue) {
        try {
            String[] parts = storedValue.split("\\$");

            if (parts.length != 6 || !"argon2id".equals(parts[1])) {
                return false;
            }

            String[] parameters = parts[3].split(",");

            int memory = Integer.parseInt(parameters[0].substring(2));
            int iterations = Integer.parseInt(parameters[1].substring(2));
            int parallelism = Integer.parseInt(parameters[2].substring(2));

            byte[] salt = Base64.getDecoder().decode(parts[4]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[5]);

            byte[] actualHash = generateHash(
                    password,
                    salt,
                    memory,
                    iterations,
                    parallelism,
                    expectedHash.length
            );

            // Constant-time comparison.
            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static byte[] generateHash(char[] password, byte[] salt) {
        return generateHash(
                password,
                salt,
                MEMORY_KB,
                ITERATIONS,
                PARALLELISM,
                HASH_LENGTH
        );
    }

    private static byte[] generateHash(
            char[] password,
            byte[] salt,
            int memory,
            int iterations,
            int parallelism,
            int hashLength
    ) {
        Argon2Parameters parameters = new Argon2Parameters.Builder(
                Argon2Parameters.ARGON2_id
        )
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withSalt(salt)
                .withMemoryAsKB(memory)
                .withIterations(iterations)
                .withParallelism(parallelism)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(parameters);

        byte[] passwordBytes =
                new String(password).getBytes(StandardCharsets.UTF_8);

        byte[] hash = new byte[hashLength];
        generator.generateBytes(passwordBytes, hash);

        java.util.Arrays.fill(passwordBytes, (byte) 0);

        return hash;
    }

    public static void main(String[] args) {
        char[] password = "StrongUserPassword123!".toCharArray();

        String storedHash = hashPassword(password);
        System.out.println("Value stored in database: " + storedHash);

        boolean valid = verifyPassword(
                "StrongUserPassword123!".toCharArray(),
                storedHash
        );

        System.out.println("Password valid: " + valid);

        java.util.Arrays.fill(password, '\0');
    }
}