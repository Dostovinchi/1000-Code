import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Secure customer registration application.
 *
 * Schema:
 * CREATE TABLE customers (
 *   id INT AUTO_INCREMENT PRIMARY KEY,
 *   username VARCHAR(50) UNIQUE NOT NULL,
 *   password_hash VARCHAR(255) NOT NULL,
 *   salt VARCHAR(64) NOT NULL,
 *   full_name VARCHAR(100) NOT NULL,
 *   email VARCHAR(100) UNIQUE NOT NULL,
 *   phone VARCHAR(20) NOT NULL
 * );
 */
public class CustomerRegistration {

    private static final Logger LOGGER = Logger.getLogger(CustomerRegistration.class.getName());

    private static final String DB_URL = "jdbc:mysql://localhost:3306/shop_db?useSSL=true&serverTimezone=UTC";
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9\\s-]{7,20}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z\\s.'-]{1,100}$");
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            System.out.print("Enter full name: ");
            String fullName = scanner.nextLine().trim();

            System.out.print("Enter email address: ");
            String email = scanner.nextLine().trim();

            System.out.print("Enter phone number: ");
            String phone = scanner.nextLine().trim();

            if (!isValid(username, USERNAME_PATTERN) ||
                !isValidPassword(password) ||
                !isValid(fullName, NAME_PATTERN) ||
                !isValid(email, EMAIL_PATTERN) ||
                !isValid(phone, PHONE_PATTERN)) {
                System.out.println("Registration failed: one or more fields are invalid.");
                return;
            }

            boolean success = registerCustomer(username, password, fullName, email, phone);
            System.out.println(success
                    ? "Registration successful! Welcome, " + fullName + "."
                    : "Registration failed. Please try again later.");
        }
    }

    private static boolean registerCustomer(String username, String password, String fullName,
                                             String email, String phone) {
        byte[] salt = generateSalt();
        String hash = hashPassword(password, salt);
        String saltEncoded = Base64.getEncoder().encodeToString(salt);

        String sql = "INSERT INTO customers (username, password_hash, salt, full_name, email, phone) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hash);
            stmt.setString(3, saltEncoded);
            stmt.setString(4, fullName);
            stmt.setString(5, email);
            stmt.setString(6, phone);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during registration", e);
            return false;
        }
    }

    private static Connection getConnection() throws SQLException {
        if (DB_USER == null || DB_PASSWORD == null) {
            throw new IllegalStateException("Database credentials are not configured.");
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static boolean isValid(String value, Pattern pattern) {
        return value != null && pattern.matcher(value).matches();
    }

    private static boolean isValidPassword(String password) {
        return password != null && !password.isEmpty() && password.length() <= MAX_PASSWORD_LENGTH;
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static String hashPassword(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Error hashing password", e);
        }
    }
}
