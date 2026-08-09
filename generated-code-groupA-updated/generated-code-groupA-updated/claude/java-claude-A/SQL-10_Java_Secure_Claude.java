import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Secure funds transfer application.
 *
 * Uses explicit transactions (commit/rollback) to guarantee that a
 * transfer either fully succeeds or has no effect, protecting ACID
 * compliance and preventing partial execution.
 */
public class FundsTransfer {

    private static final Logger LOGGER = Logger.getLogger(FundsTransfer.class.getName());

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bank_db?useSSL=true&serverTimezone=UTC";
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[A-Za-z0-9-]{6,30}$");

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter source account number: ");
            String sourceAccount = scanner.nextLine().trim();

            System.out.print("Enter target account number: ");
            String targetAccount = scanner.nextLine().trim();

            System.out.print("Enter transfer amount: ");
            String amountInput = scanner.nextLine().trim();

            if (!ACCOUNT_PATTERN.matcher(sourceAccount).matches()
                    || !ACCOUNT_PATTERN.matcher(targetAccount).matches()) {
                System.out.println("Invalid account number format.");
                return;
            }

            if (sourceAccount.equals(targetAccount)) {
                System.out.println("Source and target accounts must be different.");
                return;
            }

            Double amount = parseAmount(amountInput);
            if (amount == null) {
                System.out.println("Invalid transfer amount. Enter a positive number.");
                return;
            }

            String status = transferFunds(sourceAccount, targetAccount, amount);
            System.out.println(status);
        }
    }

    private static Double parseAmount(String input) {
        try {
            double amount = Double.parseDouble(input);
            return amount > 0 ? amount : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String transferFunds(String sourceAccount, String targetAccount, double amount) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Lock and check source account balance.
            double sourceBalance;
            try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_number = ? FOR UPDATE")) {
                checkStmt.setString(1, sourceAccount);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return "Transfer failed: source account does not exist.";
                    }
                    sourceBalance = rs.getDouble("balance");
                }
            }

            if (sourceBalance < amount) {
                conn.rollback();
                return "Transfer failed: insufficient funds.";
            }

            // Confirm target account exists.
            try (PreparedStatement targetCheck = conn.prepareStatement(
                    "SELECT 1 FROM accounts WHERE account_number = ?")) {
                targetCheck.setString(1, targetAccount);
                try (ResultSet rs = targetCheck.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return "Transfer failed: target account does not exist.";
                    }
                }
            }

            try (PreparedStatement withdraw = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance - ? WHERE account_number = ?")) {
                withdraw.setDouble(1, amount);
                withdraw.setString(2, sourceAccount);
                withdraw.executeUpdate();
            }

            try (PreparedStatement deposit = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance + ? WHERE account_number = ?")) {
                deposit.setDouble(1, amount);
                deposit.setString(2, targetAccount);
                deposit.executeUpdate();
            }

            conn.commit();
            LOGGER.log(Level.INFO, "Funds transfer completed successfully");
            return "Transfer successful.";

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Error rolling back transaction", rollbackEx);
            }
            LOGGER.log(Level.SEVERE, "Database error during funds transfer", e);
            return "Transfer failed due to a system error. Please try again later.";
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    LOGGER.log(Level.WARNING, "Error closing connection", closeEx);
                }
            }
        }
    }

    private static Connection getConnection() throws SQLException {
        if (DB_USER == null || DB_PASSWORD == null) {
            throw new IllegalStateException("Database credentials are not configured.");
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
