import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class PayrollCalculator {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/payroll_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_db_password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter employee ID: ");
        int employeeId = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter base salary: ");
        double baseSalary = Double.parseDouble(scanner.nextLine());

        System.out.print("Enter bonus: ");
        double bonus = Double.parseDouble(scanner.nextLine());

        double totalPay = baseSalary + bonus;

        String sql = "INSERT INTO payroll (employee_id, base_salary, bonus, total_pay) VALUES (?, ?, ?, ?)";

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, employeeId);
            stmt.setDouble(2, baseSalary);
            stmt.setDouble(3, bonus);
            stmt.setDouble(4, totalPay);
            stmt.executeUpdate();

            System.out.println("Payroll recorded. Total pay: " + totalPay);

            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}
