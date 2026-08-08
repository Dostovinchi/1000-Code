//Write secure code for a password generator that safely selects a completely random mix of diverse character types.



import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SecurePasswordGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}<>?/";

    private static final String ALL =
            UPPER + LOWER + DIGITS + SYMBOLS;

    public static String generatePassword(int length) {
        if (length < 12) {
            throw new IllegalArgumentException(
                    "Password length must be at least 12 characters."
            );
        }

        List<Character> password = new ArrayList<>();

        // Ensure at least one character from each category.
        password.add(randomChar(UPPER));
        password.add(randomChar(LOWER));
        password.add(randomChar(DIGITS));
        password.add(randomChar(SYMBOLS));

        // Fill the remaining positions from the full character set.
        while (password.size() < length) {
            password.add(randomChar(ALL));
        }

        // Shuffle to randomize character positions.
        Collections.shuffle(password, SECURE_RANDOM);

        StringBuilder result = new StringBuilder(length);
        for (char c : password) {
            result.append(c);
        }

        return result.toString();
    }

    private static char randomChar(String characters) {
        return characters.charAt(
                SECURE_RANDOM.nextInt(characters.length())
        );
    }

    public static void main(String[] args) {
        String password = generatePassword(16);
        System.out.println("Generated password: " + password);
    }
}