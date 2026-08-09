import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Secure payroll calculation and storage application.
 * Computes net pay from base salary, deductions, and bonus for a given
 * employee ID, and stores the result in a relational database.
 */
public class PayrollCalculator {

    private static final Logger LOGGER = Logger.getLogger(PayrollCalculator.class.getName());

    private static final String DB_URL = "jdbc:mysql://localhost:3306/payroll_db?useSSL=true&serverTimezone=UTC";
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static final double MAX_AMOUNT = 10_000_000.0;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter employee ID: ");
            Integer employeeId = parsePositiveInt(scanner.nextLine().trim());

            System.out.print("Enter base salary: ");
            Double baseSalary = parseNonNegativeAmount(scanner.nextLine().trim());

            System.out.print("Enter deductions: ");
            Double deductions = parseNonNegativeAmount(scanner.nextLine().trim());

            System.out.print("Enter bonus: ");
            Double bonus = parseNonNegativeAmount(scanner.nextLine().trim());

            if (employeeId == null || baseSalary == null || deductions == null || bonus == null) {
                System.out.println("Invalid input. Please enter valid, non-negative numeric values.");
                return;
            }

            if (deductions > baseSalary + bonus) {
                System.out.println("Invalid input: deductions cannot exceed gross pay.");
                return;
            }

            double netPay = (baseSalary + bonus) - deductions;
            String status = storePayroll(employeeId, baseSalary, deductions, bonus, netPay);
            System.out.println(status);
        }
    }

    private static Integer parsePositiveInt(String input) {
        try {
            int value = Integer.parseInt(input);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseNonNegativeAmount(String input) {
        try {
            double value = Double.parseDouble(input);
            return (value >= 0 && value <= MAX_AMOUNT) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String storePayroll(int employeeId, double baseSalary, double deductions,
                                        double bonus, double netPay) {
        String sql = "INSERT INTO payroll (employee_id, base_salary, deductions, bonus, net_pay) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setDouble(2, baseSalary);
            stmt.setDouble(3, deductions);
            stmt.setDouble(4, bonus);
            stmt.setDouble(5, netPay);
            stmt.executeUpdate();

            return "Payroll recorded successfully.";

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while storing payroll", e);
            return "Failed to record payroll. Please try again later.";
        }
    }

    private static Connection getConnection() throws SQLException {
        if (DB_USER == null || DB_PASSWORD == null) {
            throw new IllegalStateException("Database credentials are not configured.");
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
