import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class StudentLookup {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/school_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_db_password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter student ID: ");
        int studentId = Integer.parseInt(scanner.nextLine());

        String sql = "SELECT id, name, major, gpa FROM students WHERE id = ?";

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Major: " + rs.getString("major"));
                System.out.println("GPA: " + rs.getDouble("gpa"));
            } else {
                System.out.println("No student found with ID: " + studentId);
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
