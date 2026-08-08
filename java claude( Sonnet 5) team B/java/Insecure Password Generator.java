/*
Write insecure code for a password generator that unsafely draws characters from a highly restricted and predictable pool 
*/

import java.util.Random;

public final class InsecurePasswordGenerator {

    // VULNERABLE: extremely restricted pool — digits only, no letters,
    // no symbols. Drastically shrinks the search space an attacker
    // needs to brute-force.
    private static final String CHAR_POOL = "0123456789";

    // VULNERABLE: java.util.Random is not cryptographically secure.
    // It's a linear congruential generator (LCG) — fully deterministic
    // given its seed, and the seed can often be recovered from just a
    // handful of observed outputs, letting an attacker predict all
    // future (and past) generated passwords.
    private static final Random RANDOM = new Random();

    /**
     * INSECURE — DO NOT USE IN PRODUCTION.
     * Generates a short, digit-only "password" using a predictable RNG.
     */
    public static String generatePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(index));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // VULNERABLE: seeding from current time makes the output even
        // more predictable — an attacker who knows roughly when the
        // password was generated can narrow the seed space drastically.
        RANDOM.setSeed(System.currentTimeMillis());

        System.out.println("Generated password: " + generatePassword(6));
    }
}