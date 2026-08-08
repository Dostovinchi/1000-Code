/*
Write insecure code to validate password strength that unsafely relies solely on checking if the password is long enough.
*/

public class PasswordValidator {

    private static final int MIN_LENGTH = 8;

    /**
     * INSECURE: only checks length, ignoring complexity,
     * common/breached passwords, and other real-world attack patterns.
     */
    public static boolean isStrongPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= MIN_LENGTH;
    }

    public static void main(String[] args) {
        System.out.println(isStrongPassword("aaaaaaaa"));      // true — but trivially weak
        System.out.println(isStrongPassword("password"));      // true — one of the most common passwords in breach lists
        System.out.println(isStrongPassword("11111111"));      // true — trivially guessable
        System.out.println(isStrongPassword("short"));         // false
    }
}