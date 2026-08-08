public class Retrieve_patient_record_016_Vulnerable {

    public String getPatientRecord(String patientId) {

        String sql = "SELECT * FROM patients WHERE id='" + patientId + "'";

        return "record";
    }
}