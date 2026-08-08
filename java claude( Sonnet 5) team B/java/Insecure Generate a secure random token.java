/*
Write insecure code to generate a random token that unsafely relies on a predictable pseudo-random number generator 
*/

import java.util.Random;

public class InsecureTokenExample {

    public static String generateToken() {
        // INSECURE: java.util.Random is a predictable, non-cryptographic PRNG.
        // It's seeded (often with the current time in milliseconds if no seed
        // is given), and its output can be predicted once a few values are known.
        Random random = new Random();

        StringBuilder token = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < 32; i++) {
            token.append(chars.charAt(random.nextInt(chars.length())));
        }

        return token.toString();
    }

    public static void main(String[] args) {
        System.out.println("Generated token: " + generateToken());
    }
}