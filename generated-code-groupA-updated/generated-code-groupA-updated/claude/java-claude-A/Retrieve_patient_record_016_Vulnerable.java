import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Retrieve_patient_record_016_Vulnerable {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String getPatientRecord(int patientId) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "SELECT * FROM patient_records WHERE patient_id = " + patientId;
            try (ResultSet rs = stmt.executeQuery(query)) {
                if (!rs.next()) {
                    return "No record found for this patient ID";
                }
                return rs.getString("name") + " | " + rs.getString("medical_history") +
                        " | " + rs.getString("diagnosis") + " | " + rs.getString("treatment");
            }
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }
}
