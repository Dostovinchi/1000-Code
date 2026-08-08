import java.sql.*;
import java.util.Scanner;

public class TransferFundsBetweenBankAccounts {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:sample.db");
            String sql = "SELECT * FROM students WHERE username = '" + username + "' AND password = '" + password + "'";
            
            Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                System.out.println("Operation completed successfully");
            } else {
                System.out.println("No matching record found");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
