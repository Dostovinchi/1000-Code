import java.time.LocalDate;
import java.time.LocalTime;

public class Schedule_medical_appointment_017_detelid {

    public static String scheduleAppointment(
            String patientId,
            String doctorId,
            LocalDate date,
            LocalTime time) {

        // Simulate scheduling
        return "Appointment scheduled successfully for patient "
                + patientId + " with doctor "
                + doctorId + " on "
                + date + " at "
                + time + ".";
    }

    public static void main(String[] args) {
        System.out.println(scheduleAppointment(
                "P1001",
                "D2001",
                LocalDate.of(2026, 8, 15),
                LocalTime.of(10, 30)));
    }
}