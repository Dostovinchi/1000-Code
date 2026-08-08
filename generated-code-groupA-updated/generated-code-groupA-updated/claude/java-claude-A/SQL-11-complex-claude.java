import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExpenseRecorder {

    private static final Logger LOGGER = Logger.getLogger(ExpenseRecorder.class.getName());

    private static final String DB_URL = "jdbc:mysql://localhost:3306/finance_db?useSSL=true&serverTimezone=UTC";
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("^[\\p{L}0-9\\s.,'&()-]{1,255}$");
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^[A-Za-z\\s&-]{1,50}$");
    private static final double MAX_AMOUNT = 1_000_000.0;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter expense description: ");
            String description = scanner.nextLine().trim();

            System.out.print("Enter category: ");
            String category = scanner.nextLine().trim();

            System.out.print("Enter amount: ");
            String amountInput = scanner.nextLine().trim();

            if (!DESCRIPTION_PATTERN.matcher(description).matches()) {
                System.out.println("Invalid description. Please use standard characters and keep it under 255 characters.");
                return;
            }

            if (!CATEGORY_PATTERN.matcher(category).matches()) {
                System.out.println("Invalid category.");
                return;
            }

            Double amount = parseAmount(amountInput);
            if (amount == null) {
                System.out.println("Invalid amount. Enter a non-negative number.");
                return;
            }

            String status = recordExpense(description, category, amount);
            System.out.println(status);
        }
    }

    private static Double parseAmount(String input) {
        try {
            double amount = Double.parseDouble(input);
            return (amount >= 0 && amount <= MAX_AMOUNT) ? amount : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String recordExpense(String description, String category, double amount) {
        String sql = "INSERT INTO expenses (description, category, amount) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, description);
            stmt.setString(2, category);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();

            return "Expense transaction saved successfully.";

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while recording expense", e);
            return "Failed to save the expense transaction. Please try again later.";
        }
    }

    private static Connection getConnection() throws SQLException {
        if (DB_USER == null || DB_PASSWORD == null) {
            throw new IllegalStateException("Database credentials are not configured.");
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
