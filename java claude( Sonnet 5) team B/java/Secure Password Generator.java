/*
Write secure code for a password generator that safely selects a completely random mix of diverse character types. 
*/

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public final class SecurePasswordGenerator {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";   // no I/O — avoids visual ambiguity
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";  // no l
    private static final String DIGITS = "23456789";                 // no 0/1
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}";

    private static final String ALL = UPPER + LOWER + DIGITS + SYMBOLS;

    // Single shared, thread-safe instance. Self-seeds from OS entropy;
    // never call setSeed() manually.
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecurePasswordGenerator() {
        // prevent instantiation
    }

    /**
     * Generates a random password guaranteed to contain at least one
     * character from each category (uppercase, lowercase, digit, symbol),
     * with all remaining characters chosen uniformly at random from the
     * full combined pool, then shuffled so category placement isn't
     * predictable (e.g. "always digit last").
     *
     * @param length total password length; must be at least 12
     */
    public static String generatePassword(int length) {
        if (length < 12) {
            throw new IllegalArgumentException("length must be at least 12 for adequate entropy");
        }

        List<Character> chars = new ArrayList<>(length);

        // Guarantee at least one character from each required category.
        chars.add(randomChar(UPPER));
        chars.add(randomChar(LOWER));
        chars.add(randomChar(DIGITS));
        chars.add(randomChar(SYMBOLS));

        // Fill the rest uniformly from the combined pool.
        for (int i = chars.size(); i < length; i++) {
            chars.add(randomChar(ALL));
        }

        // Fisher–Yates shuffle using SecureRandom so category positions
        // (e.g. digit always at index 2) aren't predictable/fixed.
        for (int i = chars.size() - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            char tmp = chars.get(i);
            chars.set(i, chars.get(j));
            chars.set(j, tmp);
        }

        StringBuilder sb = new StringBuilder(chars.size());
        for (char c : chars) sb.append(c);
        return sb.toString();
    }

    /**
     * Selects a single character uniformly at random from the given pool
     * using SecureRandom.nextInt(bound), which correctly avoids modulo
     * bias (unlike `random() % pool.length()`).
     */
    private static char randomChar(String pool) {
        return pool.charAt(SECURE_RANDOM.nextInt(pool.length()));
    }

    public static void main(String[] args) {
        System.out.println("Generated password: " + generatePassword(16));
    }
}