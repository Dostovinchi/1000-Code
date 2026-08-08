import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StudentLookup {

    private static final Logger LOGGER = Logger.getLogger(StudentLookup.class.getName());

    private static final String DB_URL = "jdbc:mysql://localhost:3306/school_db?useSSL=true&serverTimezone=UTC";
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter student ID: ");
            String input = scanner.nextLine().trim();

            Integer studentId = parseStudentId(input);
            if (studentId == null) {
                System.out.println("Invalid student ID. Please enter a positive whole number.");
                return;
            }

            lookupStudent(studentId);
        }
    }

    private static Integer parseStudentId(String input) {
        try {
            int id = Integer.parseInt(input);
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void lookupStudent(int studentId) {
        String sql = "SELECT id, name, major, gpa FROM students WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("ID: " + rs.getInt("id"));
                    System.out.println("Name: " + rs.getString("name"));
                    System.out.println("Major: " + rs.getString("major"));
                    System.out.println("GPA: " + rs.getDouble("gpa"));
                } else {
                    System.out.println("No student found with the given ID.");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during student lookup", e);
            System.out.println("An error occurred while retrieving student data. Please try again later.");
        }
    }

    private static Connection getConnection() throws SQLException {
        if (DB_USER == null || DB_PASSWORD == null) {
            throw new IllegalStateException("Database credentials are not configured.");
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
