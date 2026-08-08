import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Secure student authentication application.
 *
 * Security measures implemented:
 *  - Parameterized SQL (PreparedStatement) everywhere - no string concatenation
 *  - Server-side input validation with a strict username allow-list pattern
 *  - Passwords are never stored or compared in plaintext (PBKDF2-HMAC-SHA256
 *    with a unique per-user salt and a high iteration count)
 *  - Generic, non-sensitive error messages shown to the user; full details
 *    are only written to an internal logger
 *  - All JDBC resources (connections, statements, result sets) are closed
 *    deterministically via try-with-resources
 */
public class P0002_SQL01_Java_Secure {

    private static final String DB_URL = "jdbc:sqlite:students_secure.db";
    private static final Logger LOGGER = Logger.getLogger(P0002_SQL01_Java_Secure.class.getName());

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,30}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    private static final int PBKDF2_ITERATIONS = 200_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            initializeDatabase();

            System.out.print("Enter username: ");
            String username = scanner.nextLine();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            try {
                validateUsername(username);
                validatePassword(password);

                boolean authenticated = authenticate(username, password);

                if (authenticated) {
                    System.out.println("Authentication successful. Welcome, " + username + "!");
                } else {
                    System.out.println("Authentication failed. Invalid username or password.");
                }
            } catch (IllegalArgumentException e) {
                // Input validation failures are safe to describe to the user.
                System.out.println("Authentication failed: " + e.getMessage());
            } catch (AuthenticationServiceException e) {
                // Internal/system errors: show only a generic message.
                System.out.println("Authentication failed: " + e.getMessage());
            }
        }
    }

    /** Custom checked exception carrying only a safe, generic message. */
    private static class AuthenticationServiceException extends Exception {
        AuthenticationServiceException(String message) {
            super(message);
        }
    }

    private static void validateUsername(String username) {
        if (username == null || !USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Username must be 3-30 characters and contain only letters, digits, or underscores.");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }
    }

    private static void initializeDatabase() {
        String createTableSql =
                "CREATE TABLE IF NOT EXISTS students (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL UNIQUE, " +
                        "password_hash TEXT NOT NULL, " +
                        "password_salt TEXT NOT NULL)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement createStmt = conn.prepareStatement(createTableSql)) {

            createStmt.executeUpdate();
            seedSampleUserIfEmpty(conn);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
            // Setup failures are logged only; the app still attempts to run,
            // and any resulting query failure is reported generically.
        }
    }

    private static void seedSampleUserIfEmpty(Connection conn) throws SQLException {
        String countSql = "SELECT COUNT(*) AS cnt FROM students";
        try (PreparedStatement countStmt = conn.prepareStatement(countSql);
             ResultSet rs = countStmt.executeQuery()) {

            if (rs.next() && rs.getInt("cnt") == 0) {
                insertSampleUser(conn, "jdoe", "CorrectHorse1!");
                insertSampleUser(conn, "asmith", "Tr0ub4dor&3!");
            }
        }
    }

    private static void insertSampleUser(Connection conn, String username, String plainPassword)
            throws SQLException {
        try {
            byte[] salt = generateSalt();
            String hash = hashPassword(plainPassword, salt);

            String insertSql =
                    "INSERT INTO students (username, password_hash, password_salt) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, hash);
                insertStmt.setString(3, Base64.getEncoder().encodeToString(salt));
                insertStmt.executeUpdate();
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.log(Level.SEVERE, "Failed to hash seed password", e);
        }
    }

    private static boolean authenticate(String username, String password)
            throws AuthenticationServiceException {

        String selectSql =
                "SELECT password_hash, password_salt FROM students WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(selectSql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    // No such user - do not reveal whether it was the
                    // username or password that was wrong.
                    return false;
                }

                String storedHash = rs.getString("password_hash");
                byte[] salt = Base64.getDecoder().decode(rs.getString("password_salt"));

                String candidateHash = hashPassword(password, salt);
                return constantTimeEquals(storedHash, candidateHash);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during authentication", e);
            throw new AuthenticationServiceException(
                    "The authentication service is temporarily unavailable. Please try again later.");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.log(Level.SEVERE, "Password hashing error during authentication", e);
            throw new AuthenticationServiceException(
                    "The authentication service is temporarily unavailable. Please try again later.");
        }
    }

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        random.nextBytes(salt);
        return salt;
    }

    private static String hashPassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    /** Constant-time string comparison to reduce timing side-channel risk. */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
