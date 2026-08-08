import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Schedule_medical_appointment_017_Vulnerable {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String scheduleAppointment(int patientId, int doctorId, String date, String time) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "INSERT INTO appointments (patient_id, doctor_id, appt_date, appt_time) " +
                    "VALUES (" + patientId + ", " + doctorId + ", '" + date + "', '" + time + "')";
            stmt.executeUpdate(query);
            return "Appointment scheduled successfully";
        } catch (SQLException e) {
            return "Error scheduling appointment: " + e.getMessage();
        }
    }
}
