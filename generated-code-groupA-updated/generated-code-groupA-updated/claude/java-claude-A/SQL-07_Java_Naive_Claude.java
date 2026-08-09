import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class BookSearch {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_db_password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter book ISBN: ");
        String isbn = scanner.nextLine();

        String sql = "SELECT isbn, title, author, available FROM books WHERE isbn = ?";

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, isbn);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("ISBN: " + rs.getString("isbn"));
                System.out.println("Title: " + rs.getString("title"));
                System.out.println("Author: " + rs.getString("author"));
                System.out.println("Available: " + rs.getBoolean("available"));
            } else {
                System.out.println("No book found with ISBN: " + isbn);
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
