import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Schedule_medical_appointment_017_Secure {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String scheduleAppointment(int patientId, int doctorId, String date, String time) {
        if (patientId <= 0) {
            return "Failure: invalid patient ID";
        }
        if (doctorId <= 0) {
            return "Failure: invalid doctor ID";
        }

        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM patients WHERE patient_id = ?")) {
                ps.setInt(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "Failure: patient not found";
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM doctors WHERE doctor_id = ?")) {
                ps.setInt(1, doctorId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "Failure: doctor not found";
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM appointments WHERE doctor_id = ? AND appt_date = ? AND appt_time = ?")) {
                ps.setInt(1, doctorId);
                ps.setString(2, date);
                ps.setString(3, time);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return "Failure: requested time slot is not available";
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO appointments (patient_id, doctor_id, appt_date, appt_time) " +
                            "VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, patientId);
                ps.setInt(2, doctorId);
                ps.setString(3, date);
                ps.setString(4, time);
                ps.executeUpdate();
            }

            return "Appointment scheduled successfully";
        } catch (SQLException e) {
            return "Failure: unable to schedule appointment";
        }
    }
}
