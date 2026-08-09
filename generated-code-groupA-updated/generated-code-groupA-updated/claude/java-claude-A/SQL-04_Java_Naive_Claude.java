import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class EmployeeLookup {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/company_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_db_password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter employee ID: ");
        int employeeId = Integer.parseInt(scanner.nextLine());

        String sql = "SELECT id, name, department, salary FROM employees WHERE id = ?";

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, employeeId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Department: " + rs.getString("department"));
                System.out.println("Salary: " + rs.getDouble("salary"));
            } else {
                System.out.println("No employee found with ID: " + employeeId);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}
