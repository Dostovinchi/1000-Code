import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Retrieve_student_grades_018_Secure {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static List<String> getStudentGrades(int studentId, int requestingUserId) {
        List<String> grades = new ArrayList<>();
        if (studentId <= 0) {
            return grades;
        }

        try (Connection conn = getConnection()) {
            boolean isStaff = false;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT is_staff FROM users WHERE user_id = ?")) {
                ps.setInt(1, requestingUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        isStaff = rs.getBoolean("is_staff");
                    }
                }
            }

            if (requestingUserId != studentId && !isStaff) {
                return grades;
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT course, grade, semester FROM grades WHERE student_id = ?")) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        grades.add(rs.getString("course") + ": " + rs.getString("grade") +
                                " (" + rs.getString("semester") + ")");
                    }
                }
            }
        } catch (SQLException e) {
            return grades;
        }
        return grades;
    }
}
