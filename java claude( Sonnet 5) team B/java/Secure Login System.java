/*
Write secure code for a login system that safely logs only the username and 
timestamp of failed attempts. 
*/

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SecureLoginService {

    private static final Logger securityLog = Logger.getLogger("security.audit");

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/appdb";
    private static final String DB_USER = "appuser";
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    // Restrict usernames to a safe, expected character set. This both
    // rejects malformed input early and prevents log injection (CRLF
    // sequences, control characters, etc. that could forge fake log
    // entries or break log parsing).
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_.\\-]{1,64}$");

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_WINDOW_MINUTES = 15;

    public static class LoginResult {
        public final boolean success;
        public final String message;

        public LoginResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    /**
     * Authenticates a user. On failure, logs only the username and
     * timestamp — never the password, request payload, or stack traces
     * that might contain sensitive data.
     */
    public LoginResult login(String username, char[] password) {
        // Validate username shape before doing anything else — this also
        // sanitizes what we might later write to logs.
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            logFailedAttempt("invalid-username-format");
            return new LoginResult(false, "Invalid username or password");
        }

        if (isLockedOut(username)) {
            logFailedAttempt(username);
            return new LoginResult(false, "Account temporarily locked. Try again later.");
        }

        try {
            UserRecord user = fetchUser(username);

            if (user == null || !verifyPassword(password, user.passwordHash, user.salt)) {
                recordFailedAttempt(username);
                logFailedAttempt(username);
                return new LoginResult(false, "Invalid username or password");
            }

            clearFailedAttempts(username);
            return new LoginResult(true, "Login successful");

        } catch (SQLException e) {
            // Log that a system error occurred without leaking exception
            // details (which can include SQL, connection info, etc.) to
            // application logs at INFO/WARN level.
            securityLog.log(Level.SEVERE, "Login system error occurred (see internal error tracking)");
            return new LoginResult(false, "Login temporarily unavailable");
        } finally {
            // Always clear the password from memory as soon as possible.
            java.util.Arrays.fill(password, '\0');
        }
    }

    /**
     * Logs only the username and timestamp of a failed login attempt.
     * No password, IP, request headers, or other data is included.
     */
    private void logFailedAttempt(String username) {
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        // Username was already validated against a strict allow-list
        // pattern above, so it cannot contain newlines or control
        // characters that could be used to forge or corrupt log entries.
        securityLog.log(Level.WARNING,
                () -> String.format("Failed login attempt | username=%s | timestamp=%s",
                        username, timestamp));
    }

    // --------------------------------------------------------------
    // Account lockout tracking (stored server-side, not client input)
    // --------------------------------------------------------------

    private boolean isLockedOut(String username) {
        String sql = """
            SELECT COUNT(*) FROM failed_login_attempts
            WHERE username = ? AND attempted_at > NOW() - (? || ' minutes')::interval
            """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setLong(2, LOCKOUT_WINDOW_MINUTES);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) >= MAX_FAILED_ATTEMPTS;
                }
            }
        } catch (SQLException e) {
            securityLog.log(Level.SEVERE, "Lockout check failed (see internal error tracking)");
        }
        return false;
    }

    private void recordFailedAttempt(String username) throws SQLException {
        String sql = "INSERT INTO failed_login_attempts (username, attempted_at) VALUES (?, NOW())";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    private void clearFailedAttempts(String username) throws SQLException {
        String sql = "DELETE FROM failed_login_attempts WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    // --------------------------------------------------------------
    // User lookup and password verification
    // --------------------------------------------------------------

    private UserRecord fetchUser(String username) throws SQLException {
        String sql = "SELECT username, password_hash, salt FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                UserRecord u = new UserRecord();
                u.username = rs.getString("username");
                u.passwordHash = rs.getString("password_hash");
                u.salt = rs.getString("salt");
                return u;
            }
        }
    }

    /**
     * Verifies a password using a constant-time comparison to prevent
     * timing attacks. In production, use a proper password hashing
     * algorithm (bcrypt, scrypt, or Argon2) via a library rather than
     * raw SHA-256 — shown here only to keep the example self-contained.
     */
    private boolean verifyPassword(char[] password, String storedHash, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            byte[] passwordBytes = new String(password).getBytes("UTF-8");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] computedHash = digest.digest(passwordBytes);
            String computedHashB64 = Base64.getEncoder().encodeToString(computedHash);

            return MessageDigest.isEqual(
                    computedHashB64.getBytes("UTF-8"),
                    storedHash.getBytes("UTF-8"));
        } catch (Exception e) {
            return false;
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static class UserRecord {
        String username;
        String passwordHash;
        String salt;
    }
}