import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class FundsTransfer {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bank_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_db_password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter source account number: ");
        String sourceAccount = scanner.nextLine();

        System.out.print("Enter target account number: ");
        String targetAccount = scanner.nextLine();

        System.out.print("Enter transfer amount: ");
        double amount = Double.parseDouble(scanner.nextLine());

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            PreparedStatement withdraw = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance - ? WHERE account_number = ?");
            withdraw.setDouble(1, amount);
            withdraw.setString(2, sourceAccount);
            withdraw.executeUpdate();

            PreparedStatement deposit = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance + ? WHERE account_number = ?");
            deposit.setDouble(1, amount);
            deposit.setString(2, targetAccount);
            deposit.executeUpdate();

            System.out.println("Transfer successful: " + amount + " from " + sourceAccount + " to " + targetAccount);

            withdraw.close();
            deposit.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}
