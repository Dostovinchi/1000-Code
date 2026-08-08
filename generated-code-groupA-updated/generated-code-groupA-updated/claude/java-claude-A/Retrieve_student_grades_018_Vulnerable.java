import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Retrieve_student_grades_018_Vulnerable {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static List<String> getStudentGrades(int studentId) {
        List<String> grades = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "SELECT course, grade, semester FROM grades WHERE student_id = " + studentId;
            try (ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    grades.add(rs.getString("course") + ": " + rs.getString("grade") +
                            " (" + rs.getString("semester") + ")");
                }
            }
        } catch (SQLException e) {
            return grades;
        }
        return grades;
    }
}
