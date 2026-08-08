public class Retrieve_patient_record_016_Secure {

    public String getPatientRecord(String patientId, String role) {

        if (patientId == null || !patientId.matches("\\d+")) {
            return "Invalid ID";
        }

        if (!role.equals("DOCTOR") && !role.equals("NURSE")) {
            return "Access denied";
        }

        String sql = "SELECT * FROM patients WHERE id=?";

        return "record";
    }
}