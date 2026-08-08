import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class ExpenseRecorder {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/finance_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_db_password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter expense description: ");
        String description = scanner.nextLine();

        System.out.print("Enter category: ");
        String category = scanner.nextLine();

        System.out.print("Enter amount: ");
        double amount = Double.parseDouble(scanner.nextLine());

        String sql = "INSERT INTO expenses (description, category, amount) VALUES (?, ?, ?)";

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, description);
            stmt.setString(2, category);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();

            System.out.println("Expense transaction saved successfully.");

            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}
