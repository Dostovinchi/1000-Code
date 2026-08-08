//Write insecure code for a password generator that unsafely draws characters from a highly restricted and predictable pool


import java.util.Random;

public class InsecurePasswordGenerator {

    private static final String WEAK_POOL = "abc123";
    private static final Random RANDOM = new Random();

    public static String generatePassword(int length) {
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(WEAK_POOL.length());
            password.append(WEAK_POOL.charAt(index));
        }

        return password.toString();
    }

    public static void main(String[] args) {
        System.out.println("Generated password: " + generatePassword(8));
    }
}