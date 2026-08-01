import java.util.HashMap;
import java.util.Map;

public class Retrieve_student_grades_018_detelid {

    public static String getStudentGrades(String studentId) {

        Map<String, String> records = new HashMap<>();

        records.put("S1001",
                "Semester: Spring 2026\nMath: A\nPhysics: B+\nProgramming: A-");

        return records.getOrDefault(
                studentId,
                "No student record found.");
    }

    public static void main(String[] args) {
        System.out.println(getStudentGrades("S1001"));
    }
}