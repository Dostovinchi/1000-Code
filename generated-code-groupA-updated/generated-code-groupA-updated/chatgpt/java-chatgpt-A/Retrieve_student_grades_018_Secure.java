import java.util.HashMap;
import java.util.Map;

public class Retrieve_student_grades_018_Secure {

    private static final Map<String, String> records = new HashMap<>();

    static {
        records.put("S1001",
                "Semester: Spring 2026\nMath: A\nPhysics: B+\nProgramming: A-");
    }

    public static String getStudentGrades(
            String studentId,
            String requesterId,
            boolean authorizedStaff) {

        if (studentId == null || studentId.isBlank())
            return "Invalid student ID.";

        if (!authorizedStaff && !studentId.equals(requesterId))
            return "Access denied.";

        return records.getOrDefault(
                studentId,
                "No student record found.");
    }

    public static void main(String[] args) {
        System.out.println(getStudentGrades(
                "S1001",
                "S1001",
                false));
    }
}