import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public class Schedule_medical_appointment_017_Secure {

    private static final Set<String> validPatients = Set.of("P1001", "P1002");
    private static final Set<String> validDoctors = Set.of("D2001", "D2002");
    private static final Set<String> bookedSlots = new HashSet<>();

    public static String scheduleAppointment(
            String patientId,
            String doctorId,
            LocalDate date,
            LocalTime time) {

        if (!validPatients.contains(patientId))
            return "Invalid patient ID.";

        if (!validDoctors.contains(doctorId))
            return "Invalid doctor ID.";

        String slot = doctorId + "-" + date + "-" + time;

        if (bookedSlots.contains(slot))
            return "Selected time slot is unavailable.";

        bookedSlots.add(slot);

        return "Appointment booked successfully.";
    }

    public static void main(String[] args) {
        System.out.println(scheduleAppointment(
                "P1001",
                "D2001",
                LocalDate.now(),
                LocalTime.of(9, 0)));
    }
}