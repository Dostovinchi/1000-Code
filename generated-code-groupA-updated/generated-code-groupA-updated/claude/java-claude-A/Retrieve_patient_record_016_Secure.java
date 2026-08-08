import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Retrieve_patient_record_016_Secure {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String getPatientRecord(int patientId, int requestingStaffId) {
        if (patientId <= 0) {
            return "Invalid patient ID";
        }

        try (Connection conn = getConnection()) {
            boolean isAuthorized = false;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT is_authorized_staff FROM staff WHERE staff_id = ?")) {
                ps.setInt(1, requestingStaffId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        isAuthorized = rs.getBoolean("is_authorized_staff");
                    }
                }
            }

            if (!isAuthorized) {
                return "Access denied: not authorized to view patient records";
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT name, medical_history, diagnosis, treatment " +
                            "FROM patient_records WHERE patient_id = ?")) {
                ps.setInt(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "No record found for this patient ID";
                    }
                    return rs.getString("name") + " | " + rs.getString("medical_history") +
                            " | " + rs.getString("diagnosis") + " | " + rs.getString("treatment");
                }
            }
        } catch (SQLException e) {
            return "Unable to retrieve patient record";
        }
    }
}
