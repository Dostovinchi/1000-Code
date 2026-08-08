//Write insecure code to store passwords that unsafely saves them using a simple, unkeyed hash



import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class InsecurePasswordStorage {

    public static String hashPassword(String password) throws Exception {
        // INSECURE: SHA-256 is fast, unkeyed, and uses no unique salt.
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

        return HexFormat.of().formatHex(hash);
    }

    public static void main(String[] args) throws Exception {
        String password = "UserPassword123";
        String storedHash = hashPassword(password);

        // Represents saving the value directly in a database.
        System.out.println("Stored password hash: " + storedHash);
    }
}